package io.qrpc.consumer.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.common.threadPool.ClientThreadPool;
import io.qrpc.common.utils.IpUtils;
import io.qrpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.qrpc.consumer.common.manager.ConsumerConnectionManager;
import io.qrpc.loadBalancer.context.ConnectionsContext;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.consumer.common.handler.RpcConsumerHandler;
import io.qrpc.consumer.common.initializer.RpcConsumerInitializer;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RpcConsumer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:37
 * @Description: 消费者启动端
 * 44章，增加源IP成员变量，构造时赋值
 * 7节，增加三个用于发送ping消息的成员变量
 */

//实现Consumer接口的sendRequest方法
public class RpcConsumer implements Consumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcConsumer.class);

    //连接Netty服务端用
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    //单例
    private static volatile RpcConsumer instance;

    //负载均衡-源IP哈希算法需要传入的参数，构造方法中赋值
    private final String localIp;

    //7节，用于定时发送ping消息给提供者
    private ScheduledExecutorService executorService;
    private int heartbeatInterval = 3000; //默认心跳间隔30秒
    private int scanNotActiveChannelInterval = 60000; //默认扫描移除空闲连接间隔60秒

    //8节，引入重试机制
    private int maxRetryTimes = 3; //最大重试次数
    private int retryInterval = 1000;  //重试间隔时间
    private volatile int curRetryTimes = 0; //当前重试次数

    /**
     * @author: qiu
     * @date: 2024/3/3 23:00
     * @description: 7节增加构造参数，由用户配置心跳间隔时间和移除非活跃连接的间隔时间
     */
    private RpcConsumer(
            int heartbeatInterval,
            int scanNotActiveChannelInterval,
            int maxRetryTimes,
            int retryInterval
    ) {
        //小于0则使用默认间隔时间
        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        if (scanNotActiveChannelInterval > 0)
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;

        //重试参数如果小于零就取默认值
        this.maxRetryTimes = maxRetryTimes > 0 ? maxRetryTimes : this.maxRetryTimes;
        this.retryInterval = retryInterval > 0 ? retryInterval : this.retryInterval;

        localIp = IpUtils.getLocalHostIp();

        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer(this.heartbeatInterval));

        //7节，开启自定义心跳机制
//        this.startHeartBeat();
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 0:09
     * @description: 单例获取方法
     * 7节、8节修改，增加参数
     */
    public static RpcConsumer getInstance(
            int heartbeatInterval,
            int scanNotActiveChannelInterval,
            int maxRetryTimes,
            int retryInterval
    ) {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) instance = new RpcConsumer(
                        heartbeatInterval,
                        scanNotActiveChannelInterval,
                        maxRetryTimes,
                        retryInterval
                );
            }
        }
        return instance;
    }

    /**
     * @author: qiu
     * @date: 2024/2/25 13:46
     * @description: 发送请求，后面由代理类调用
     * 23章修改，引入注册中心，通过服务与发现接口根据请求参数获取服务元数据，根据服务元数据获取handler来发送请求
     * 8节，引入重试机制，发现服务失败和连接服务提供者失败都重试
     */
    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        LOGGER.info("RpcConsumer#sendRequest消费者端发送请求...");
        RpcRequest request = protocol.getBody();
        //获取请求要调用的远程方法所在类、版本、分组
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] parameters = request.getParameters();

        //invokerHashcode使用第一个参数的哈希值，没有参数就用serviceKey的哈希值。后续可以使用其他的，没有任何关系
        int invokerHashcode = parameters == null ? serviceKey.hashCode() : parameters[0].hashCode();

        //引入重试机制，根据serviceKey和invokerHashcode通过接口获取服务元数据
        ServiceMeta serviceMeta = this.discoveryServiceWithRetry(registryService, serviceKey, invokerHashcode);

        //连接提供者获取ConsumerHanlder来发送请求和接收结果。如果meta为null，执行返回结果也是null。
        RpcConsumerHandler consumerHandler = this.connectNettyServerWithRetry(serviceMeta);
        RpcFuture future = null;
        if (consumerHandler != null) {
            future = consumerHandler.sendRequst(protocol, request.isAsync(), request.isOneway());
        }
        return future;
    }

    /**
     * @author: qiu
     * @date: 2024/3/5 10:21
     * @description: 8节新增，获取服务，失败后进行重试，否则返回服务。
     */
    private ServiceMeta discoveryServiceWithRetry(RegistryService registryService, String serviceKey, int invokerHashcode) throws Exception {
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashcode, localIp);
        if (serviceMeta == null) {
            for (int i = 0; i < maxRetryTimes; i++) {
                LOGGER.error("获取服务出错！准备进行第【{}】次尝试重新获取...",i+1);
                serviceMeta = registryService.discovery(serviceKey, invokerHashcode, localIp);

                if (serviceMeta != null) break;

                Thread.sleep(retryInterval);
            }
            LOGGER.error("服务获取失败！没有这样的服务！");
        }
        return serviceMeta;
    }

    /**
     * @author: qiu
     * @date: 2024/3/5 10:56
     * @description: 8节新增，引入重试机制，先从缓存中看有没有handler，有说明之前已经连接成功了，直接返回handler,否则先连接，连接失败则进入重试
     */
    private RpcConsumerHandler connectNettyServerWithRetry(ServiceMeta serviceMeta) throws InterruptedException {
        RpcConsumerHandler consumerHandler = null;
        if (serviceMeta != null) {
            try {
                //先试图从缓存中找有没有需要的handler，如果没有就getRpcConsumerHandler建立连接并获取
                consumerHandler = RpcConsumerHandlerHelper.get(serviceMeta);
                if (consumerHandler == null) {
                    consumerHandler = connectNettyServer(serviceMeta);
                    RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
                } else if (!consumerHandler.getcHannel().isActive()) { //缓存中存在但是不活跃，TODO 待进一步理解
                    consumerHandler.close();
                    consumerHandler = connectNettyServer(serviceMeta);
                    RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
                }
            }catch (Exception e){
                //连接失败，进入重试阶段
                if (e instanceof ConnectException){
                    if (consumerHandler == null){
                        if (curRetryTimes == maxRetryTimes)
                            LOGGER.error("连接Netty服务端{}:{}出错!",serviceMeta.getServiceAddr(),serviceMeta.getServicePort());
                        if (curRetryTimes < maxRetryTimes){
                            curRetryTimes++;
                            LOGGER.error("连接Netty服务端{}:{}出错!准备进行第【{}】次连接重试...",serviceMeta.getServiceAddr(),serviceMeta.getServicePort(),curRetryTimes);
                            consumerHandler = this.connectNettyServerWithRetry(serviceMeta);
                            Thread.sleep(retryInterval);
                        }
                    }
                }
            }
            return consumerHandler;
        }
        return null;
    }


    /**
     * @author: qiu
     * @date: 2024/3/1 19:54
     * @description: 与Netty服务端建立连接，并返回handler
     * 6.5节修改，参数修改为ServiceMeta，连接成功后应将连接数的信息保存到ConnectionContext里
     */
    private RpcConsumerHandler connectNettyServer(ServiceMeta meta) throws InterruptedException {
        LOGGER.info("RpcConsumer#getRpcConsumerHandler连接Netty服务端中...");
        ChannelFuture future;
        future = bootstrap.connect(meta.getServiceAddr(), meta.getServicePort()).sync();
        future.addListener((ChannelFutureListener) listener -> {
            if (future.isSuccess()) {
                LOGGER.info("连接Netty服务端{}:{}成功！", meta.getServiceAddr(), meta.getServicePort());
                //6.5节，连接成功后，连接数+1
                ConnectionsContext.addConnection(meta);
            } else {
                //8节，引入重试机制直接抛出异常更好
//                LOGGER.error("连接Netty服务端{}:{}失败", meta.getServiceAddr(), meta.getServicePort());
//                future.cause().printStackTrace();
//                eventLoopGroup.shutdownGracefully();
                throw new ConnectException();
            }
        });

        return future.channel().pipeline().get(RpcConsumerHandler.class);
    }

    /**
     * @author: qiu
     * @date: 2024/2/25 13:56
     * @description: 关闭消费者的Netty客户端连接
     * 3.5节新增，关闭线程池
     * 23章修改，关闭Handler
     */
    public void close() {
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdowm();
    }

    /**
     * @author: qiu
     * @date: 2024/3/5 12:35
     * @description: 7节新增，心跳机制启动，一个定时任务移除连接缓存，一个定时任务发送心跳
     */
    private void startHeartBeat() {
        executorService = Executors.newScheduledThreadPool(2);

        executorService.scheduleAtFixedRate(() -> {
            LOGGER.info("消费者{}正在扫描清理非活跃连接...", IpUtils.getLoacalInetAddress());
            ConsumerConnectionManager.removeNotActiveChannel();
        }, 10, this.scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(ConsumerConnectionManager::sendPingFromConsumer, 3, this.heartbeatInterval, TimeUnit.MILLISECONDS);
    }
}
