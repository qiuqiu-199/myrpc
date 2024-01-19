package io.qrpc.test.consumer.codec.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RpcTestConsumerHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/19 9:12
 * @Description: 模拟发送消费者数据临时用
 */

public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcTestConsumerHandler.class);


    //在成功建立连接时触发，我们是模拟，所以直接在建立链接的时候就发送模拟数据给消费者
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("=============模拟消费者发送数据：");

        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest requst = new RpcRequest();
        requst.setClassName("io.qrpc.test.api.DemoService");
        requst.setVersion("1.0.0");
        requst.setGroup("qiu");
        requst.setMethodName("hello");
        requst.setParameterTypes(new Class[]{String.class});
        requst.setParameters(new Object[]{"qiu"});
        requst.setAsync(false);
        requst.setOneway(false);
        protocol.setBody(requst);
        LOGGER.info("消费者模拟的发送数据为：==》{}",JSONObject.toJSONString(protocol));

        ctx.writeAndFlush(protocol);
        LOGGER.info("消费者模拟数据发送完毕！");
    }

    //处理对方发送过来的数据
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> protocol) throws Exception {
            LOGGER.info("服务提供者收到数据===》{}", JSONObject.toJSONString(protocol));
    }
}
