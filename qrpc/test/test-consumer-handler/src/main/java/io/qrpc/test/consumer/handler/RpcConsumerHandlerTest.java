package io.qrpc.test.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import io.qrpc.consumer.common.RpcConsumer;
import io.qrpc.consumer.common.future.RpcFuture;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * @ClassName: RpcConsumerHandlerTest
 * @Author: qiuzhiq
 * @Date: 2024/1/19 18:00
 * @Description:
 */

public class RpcConsumerHandlerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RpcConsumer consumer = RpcConsumer.getInstance();

        //接收结果
        RpcFuture future = consumer.sendRequest(getProtocol());
        LOGGER.info("消费者模拟数据发送完毕！");
        LOGGER.info("消费者返回的数据为：{}",future.get());

        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getProtocol() {
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
        LOGGER.info("消费者模拟的发送数据为：==》{}", JSONObject.toJSONString(protocol));

        return protocol;
    }
}
