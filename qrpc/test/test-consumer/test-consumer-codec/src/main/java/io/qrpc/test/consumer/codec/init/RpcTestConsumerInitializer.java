package io.qrpc.test.consumer.codec.init;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.qrpc.codec.RpcDecoder;
import io.qrpc.codec.RpcEncoder;
import io.qrpc.test.consumer.codec.handler.RpcTestConsumerHandler;

/**
 * @ClassName: RpcTestConsumerInitializer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 9:09
 * @Description: 初始化器罢了，不用新建这个类也行
 */

public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new RpcEncoder())
                .addLast(new RpcDecoder())
                .addLast(new RpcTestConsumerHandler());
    }
}
