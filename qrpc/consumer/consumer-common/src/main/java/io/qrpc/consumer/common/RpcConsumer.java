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

/**
 * @ClassName: RpcConsumer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:37
 * @Description: 消费者启动端
 * 44章，增加源IP成员变量，构造时赋值
 */

//实现Consumer接口的sendRequest方法
public class RpcConsumer implements Consumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcConsumer.class);

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;
    private final String localIp;

    //缓存当前消费者与服务端的连接
    //TODO 一个handler是一个连接吗
    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer() {
        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
    }

    public static RpcConsumer getInstance() {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) instance = new RpcConsumer();
            }
        }
        return instance;
    }


    /**
     * @author: qiu
     * @date: 2024/2/25 13:46
     * @param: null
     * @return: null
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
                consumerHandler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
            } else if (!consumerHandler.getcHannel().isActive()) { //缓存中存在但是不活跃，TODO 待进一步理解
                consumerHandler.close();
                consumerHandler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
            }
            //根据请求选择的调用方式选择对应的调用方式（同步、异步和单向调用）
            return consumerHandler.sendRequst(protocol, request.isAsync(), request.isOneway());
        }
        return null;
    }

    //获取handler，与Netty服务端建立连接
    private RpcConsumerHandler getRpcConsumerHandler(String ip, int port) throws InterruptedException {
        LOGGER.info("RpcConsumer#getRpcConsumerHandler连接Netty服务端中...");
        ChannelFuture future;
        future = bootstrap.connect(ip, port).sync();
        future.addListener((ChannelFutureListener) listener -> {
            if (future.isSuccess()) LOGGER.info("连接Netty服务端{}:{}成功！", ip, port);
            else {
                LOGGER.error("连接Netty服务端{}:{}失败", ip, port);
                future.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return future.channel().pipeline().get(RpcConsumerHandler.class);
    }

    //关闭消费者

    /**
     * @author: qiu
     * @date: 2024/2/25 13:56
     * @param:
     * @return: void
     * @description: 关闭消费者的Netty客户端连接
     * 3.5节新增，关闭线程池
     * 23章修改，关闭Handler
     */
    public void close() {
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdowm();
    }
}
