package io.qrpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.consumer.common.context.RpcContext;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: RpcConsumerHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:19
 * @Description: 消费者的handler
 */

public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;  //TODO 为什么要volatile修饰？
    private SocketAddress remotePeer;  //TODO 作用？目前并没有用上

    //存储请求id与对应的Response的映射关系，当一次请求产生结果就将对应的response放在这里，可以从这里得到请求的结果
//    private Map<Long, RpcProtocol<RpcResponse>> pendingResponseMap = new ConcurrentHashMap<>();
    private Map<Long, RpcFuture> pendingRpc = new ConcurrentHashMap<>();

    public Channel getcHannel() {
        LOGGER.info("RpcConsumerHandler#getcHannel...");
        return channel;

    }

    public SocketAddress getRemotePeer() {
        LOGGER.info("RpcConsumerHandler#getRemotePeer...");
        return remotePeer;
    }

    //注册连接时触发，获取channel
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("RpcConsumerHandler#channelRegistered...");
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    //激活连接时触发，获取SocketAddress
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("RpcConsumerHandler#channelActive...");
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    //消费者接收提供者返回的数据时触发，对接收数据进行处理
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) {
        LOGGER.info("RpcConsumerHandler#channelRead0...");
        if (protocol == null) return;

        LOGGER.info("在Handler里消费者接收到响应数据：{}", JSONObject.toJSONString(protocol));

        long requestId = protocol.getHeader().getRequestId();
        RpcFuture rpcFuture = pendingRpc.remove(requestId);
//        消费者接收到来自提供者的存入结果存入future中
        if (rpcFuture != null) {
            rpcFuture.done(protocol);
            LOGGER.info("RpcConsumerHandler#channelRead0消费者接收到来自提供者的结果存入future中...");
        }
    }


    //消费者向提供者发送数据
    public RpcFuture sendRequst(RpcProtocol<RpcRequest> protocol,boolean isAsync, boolean isOneWay) {
        LOGGER.info("RpcConsumerHandler#sendRequst...");

//        channel.writeAndFlush(protocol);  注：3.4-3.5bug根源，见记录

        return isAsync ? this.sendRequestAsync(protocol) :
                isOneWay ? this.sendRequestOneway(protocol) : this.sendRequestSync(protocol);
    }

    //消费者向提供者发送数据，同步调用
    private RpcFuture sendRequestSync(RpcProtocol<RpcRequest> protocol){
        LOGGER.info("RpcConsumerHandler#sendRequstSync同步调用中...消费者准备发送的数据：{}", JSONObject.toJSONString(protocol));
        //取消while循环获取响应结果，使用RpcFuture来存放响应结果
        RpcFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }
    //消费者向提供者发送数据，异步调用
    private RpcFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        LOGGER.info("RpcConsumerHandler#sendRequstAsync异步调用中...消费者准备发送的数据：{}", JSONObject.toJSONString(protocol));

        //异步调用将future存入上下文RpcContext里
        RpcFuture future = this.getRpcFuture(protocol);
        RpcContext.getContext().setFuture(future);
        channel.writeAndFlush(protocol);

        return null;
    }
    //消费者向提供者发送数据，单向调用
    private RpcFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        LOGGER.info("RpcConsumerHandler#sendRequstOneway单向调用中...消费者准备发送的数据：{}", JSONObject.toJSONString(protocol));
        channel.writeAndFlush(protocol);
        return null;
    }

    private RpcFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        LOGGER.info("RpcConsumerHandler#getRpcFuture...");
        //在这里创建好请求对应的future存入map里
        RpcFuture future = new RpcFuture(protocol);
        pendingRpc.put(protocol.getHeader().getRequestId(), future);

        return future;
    }


    //TODO 作用以及为什么这么写不理解
    public void close() {
        LOGGER.info("RpcConsumerHandler#close...");
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
