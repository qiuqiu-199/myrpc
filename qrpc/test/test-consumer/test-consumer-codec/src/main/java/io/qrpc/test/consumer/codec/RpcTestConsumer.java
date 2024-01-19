package io.qrpc.test.consumer.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.qrpc.test.consumer.codec.init.RpcTestConsumerInitializer;

/**
 * @ClassName: RpcTestConsumer
 * @Author: qiuzhiq
 * @Date: 2024/1/19 9:26
 * @Description: 测试消费者发送数据的模拟
 */

public class RpcTestConsumer {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup(4);
        try{
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcTestConsumerInitializer());

            bootstrap.connect("127.0.0.1",27880).sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //线程休眠两秒再关闭服务提供者，否则会导致消费者和服务者在进行数据交互时关闭消费者会抛异常
            Thread.sleep(2000);
            System.out.println("0000");
            eventExecutors.shutdownGracefully();
        }
    }
}
