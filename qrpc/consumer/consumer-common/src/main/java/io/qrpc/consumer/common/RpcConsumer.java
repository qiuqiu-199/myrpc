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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private int scanNotActiveChannelInterval = 6000; //默认扫描移除空闲连接间隔60秒

    /**
     * @author: qiu
     * @date: 2024/3/3 23:00
     * @description: 7节增加构造参数，由用户配置心跳间隔时间和移除非活跃连接的间隔时间
     */
    private RpcConsumer(int heartbeatInterval,int scanNotActiveChannelInterval) {
        //小于0则使用默认间隔时间
        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        if (scanNotActiveChannelInterval > 0)
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;

        localIp = IpUtils.getLocalHostIp();

        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer(heartbeatInterval));

        //7节，开启心跳机制
        this.startHeartBeat();
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 0:09
     * @description: 单例获取方法
     * 7节修改，增加参数
     */
    public static RpcConsumer getInstance(int heartbeatInterval,int scanNotActiveChannelInterval) {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) instance = new RpcConsumer(heartbeatInterval,scanNotActiveChannelInterval);
            }
        }
        return instance;
    }

    /**
     * @author: qiu
     * @date: 2024/2/25 13:46
     * @description: 发送请求，后面由代理类调用
     * 23章修改，引入注册中心，通过服务与发现接口根据请求参数获取服务元数据，根据服务元数据获取handler来发送请求
     */
    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        LOGGER.info("RpcConsumer#sendRequest消费者端发送请求...");
        RpcRequest request = protocol.getBody();
        //获取请求要调用的远程方法所在类、版本、分组
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] parameters = request.getParameters();

        //invokerHashcode使用第一个参数的哈希值，没有参数就用serviceKey的哈希值
        int invokerHashcode = parameters == null ? serviceKey.hashCode() : parameters[0].hashCode();

        //根据serviceKey和invokerHashcode通过接口获取服务元数据serviceMeta
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashcode,localIp);

        //根据serviceMeta获取对应的ConsumerHandler，通过Handler发送请求并接收结果
        if (serviceMeta != null) {
            RpcConsumerHandler consumerHandler = RpcConsumerHandlerHelper.get(serviceMeta);
            if (consumerHandler == null) {
                consumerHandler = getRpcConsumerHandler(serviceMeta);
                RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
            } else if (!consumerHandler.getcHannel().isActive()) { //缓存中存在但是不活跃，TODO 待进一步理解
                consumerHandler.close();
                consumerHandler = getRpcConsumerHandler(serviceMeta);
                RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
            }
            //根据请求选择的调用方式选择对应的调用方式（同步、异步和单向调用）
            return consumerHandler.sendRequst(protocol, request.isAsync(), request.isOneway());
        }
        return null;
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 19:54
     * @description: 与Netty服务端建立连接，并返回handler
     * 6.5节修改，参数修改为ServiceMeta，连接成功后应将连接数的信息保存到ConnectionContext里
     */
    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta meta) throws InterruptedException {
        LOGGER.info("RpcConsumer#getRpcConsumerHandler连接Netty服务端中...");
        ChannelFuture future;
        future = bootstrap.connect(meta.getServiceAddr(), meta.getServicePort()).sync();
        future.addListener((ChannelFutureListener) listener -> {
            if (future.isSuccess()) {
                LOGGER.info("连接Netty服务端{}:{}成功！", meta.getServiceAddr(), meta.getServicePort());
                //6.5节，连接成功后，连接数+1
                ConnectionsContext.addConnection(meta);
            }
            else {
                LOGGER.error("连接Netty服务端{}:{}失败", meta.getServiceAddr(), meta.getServicePort());
                future.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
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

    private void startHeartBeat(){
        executorService = Executors.newScheduledThreadPool(2);

        executorService.scheduleAtFixedRate(()->{
            LOGGER.info("消费者{}正在扫描清理非活跃连接...",IpUtils.getLoacalInetAddress());
            ConsumerConnectionManager.removeNotActiveChannel();
        },10,this.scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(ConsumerConnectionManager::sendPingFromConsumer,3,this.heartbeatInterval,TimeUnit.MILLISECONDS);
    }
}
