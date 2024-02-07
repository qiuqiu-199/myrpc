package io.qrpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.consumer.common.future.RpcFuture;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeader;
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
        return channel;

    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    //注册连接时触发，获取channel
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    //激活连接时触发，获取SocketAddress
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    //消费者接收提供者返回的数据时触发，对接收数据进行处理
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) {
        if (protocol == null) return;

        LOGGER.info("消费者接收到响应数据：{}", JSONObject.toJSONString(protocol));

        long requestId = protocol.getHeader().getRequestId();
        RpcFuture rpcFuture = pendingRpc.remove(requestId);
//        消费者接收到来自提供者的存入结果存入future中
        if (rpcFuture != null) {
            rpcFuture.done(protocol);
            LOGGER.info("消费者接收到来自提供者的存入结果存入rpc中");
        }
    }


    //消费者向提供者发送数据
    public RpcFuture sendRequst(RpcProtocol<RpcRequest> protocol) {
        LOGGER.info("消费者准备发送的数据：{}", JSONObject.toJSONString(protocol));
        channel.writeAndFlush(protocol);

        //取消while循环获取响应结果，使用RpcFuture来存放响应结果
        return this.getRpcFuture(protocol);
    }

    private RpcFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        //在这里创建好请求对应的future存入map里
        RpcFuture future = new RpcFuture(protocol);
        pendingRpc.put(protocol.getHeader().getRequestId(),future);

        return future;
    }


    //TODO 作用以及为什么这么写不理解
    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
