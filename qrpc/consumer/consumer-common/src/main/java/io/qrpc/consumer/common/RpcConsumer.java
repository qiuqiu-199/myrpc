package io.qrpc.consumer.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.qrpc.consumer.common.future.RpcFuture;
import io.qrpc.consumer.common.handler.RpcConsumerHandler;
import io.qrpc.consumer.common.initializer.RpcConsumerInitializer;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: RpcConsumer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:37
 * @Description: 消费者启动端
 */

public class RpcConsumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcConsumer.class);

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;

    //缓存当前消费者与服务端的连接
    //TODO 一个handler是一个连接吗
    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer() {
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


    //发送请求，后面由代理类调用
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws InterruptedException {
        // TODO 暂时写死，后续引入注册中心后更新
        String ip = "127.0.0.1";
        int port = 27880;

        String key = String.join("_", ip, String.valueOf(port));
        RpcConsumerHandler handler = handlerMap.get(key);

        //
        if (handler == null) {
            handler = getRpcConsumerHandler(ip, port);
            handlerMap.put(key, handler);
        } else if (!handler.getcHannel().isActive()) {  //TODO 缓存中存在但是不活跃？？
            handler.close();
            handler = getRpcConsumerHandler(ip, port);
            handlerMap.put(key, handler);
        }

        //根据请求选择的调用方式选择对应的调用方式（同步、异步和单向调用）
        RpcRequest request = protocol.getBody();
        return handler.sendRequst(protocol,request.isAsync(),request.isOneway());
    }

    //获取handler
    private RpcConsumerHandler getRpcConsumerHandler(String ip, int port) throws InterruptedException {
        ChannelFuture future;
        future = bootstrap.connect(ip, port).sync();
        future.addListener((ChannelFutureListener) listener -> {
            if (future.isSuccess()) LOGGER.info("连接服务端{}:{}成功！", ip, port);
            else {
                LOGGER.error("连接服务端{}:{}失败", ip, port);
                future.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return future.channel().pipeline().get(RpcConsumerHandler.class);
    }

    //关闭消费者
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
