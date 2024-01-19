package io.qrpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * @ClassName: RpcConsumerHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:19
 * @Description: 消费者的handler
 */

public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;  //TODO 为什么要volatile修饰？
    private SocketAddress remotePeer;

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

    //接收数据时触发，对接收数据进行处理
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> rpcResponseRpcProtocol) throws Exception {
        LOGGER.info("接受到响应数据：{}", JSONObject.toJSONString(rpcResponseRpcProtocol));
    }


    //消费者向提供者发送数据
    public void sendRequst(RpcProtocol<RpcRequest> protocol){
        LOGGER.info("消费者准备发送的数据：{}",JSONObject.toJSONString(protocol));
        channel.writeAndFlush(protocol);
    }


    //TODO 作用以及为什么这么写不理解
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
