package io.qrpc.test.consumer.handler;

import io.qrpc.consumer.common.RpcConsumer;
import io.qrpc.proxy.api.callback.AsyncRpcCallback;
import io.qrpc.proxy.api.future.RpcFuture;
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
        //回调方法传递
        future.addCallback(new AsyncRpcCallback() {
            @Override
            public void onSuccess(Object res) {
                LOGGER.info("回调方法执行成功，消费者返回的数据为：{}",res);
            }

            @Override
            public void onException(Exception e) {
                LOGGER.info("回调方法执行失败，抛出异常：{}",e.toString());
            }
        });


        //测试异步调用
//        RpcFuture future_async = RpcContext.getContext().getFuture();
//        LOGGER.info("异步调用结果：{}",future_async.get());

        //测试单向调用
//        LOGGER.info("单向调用...");

        Thread.sleep(200);
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

        return protocol;
    }
}
