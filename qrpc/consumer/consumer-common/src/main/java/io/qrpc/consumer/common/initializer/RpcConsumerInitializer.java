package io.qrpc.consumer.common.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.qrpc.codec.RpcDecoder;
import io.qrpc.codec.RpcEncoder;
import io.qrpc.consumer.common.handler.RpcConsumerHandler;

/**
 * @ClassName: RpcConsumerInitializer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 17:35
 * @Description: 消费者的初始化器
 */

public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcConsumerHandler());
    }
}
