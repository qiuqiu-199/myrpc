package io.qrpc.consumer.common.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.qrpc.codec.RpcDecoder;
import io.qrpc.codec.RpcEncoder;
import io.qrpc.constants.RpcConstants;
import io.qrpc.consumer.common.handler.RpcConsumerHandler;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RpcConsumerInitializer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:35
 * @Description: 消费者的初始化器
 * 7节修改，增加IdleStateHandler处理超时事件
 */

public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    private int heartbeatInterval;

    /**
     * @author: qiu
     * @date: 2024/3/4 11:10
     * @description: 7节新增，增加构造参数
     */
    public RpcConsumerInitializer(int heartbeatInterval){
        this.heartbeatInterval = heartbeatInterval;
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 11:11
     * @description: 7节修改，增加空闲状态处理器
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(RpcConstants.CODEC_ENCODER,new RpcEncoder())
                                .addLast(RpcConstants.CODEC_DEVODER,new RpcDecoder())
                                .addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER,new IdleStateHandler(0,0,heartbeatInterval, TimeUnit.MILLISECONDS))
                                .addLast(RpcConstants.CODEC_HANDLER,new RpcConsumerHandler());
    }
}
