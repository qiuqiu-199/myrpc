package io.test.consumer;

import io.qrpc.consumer.RpcClient;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * @ClassName: RpcConsumerTestNative
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:32
 * @Description:
 */

public class RpcConsumerTestNative {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerTestNative.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        RpcClient client = new RpcClient("1.0.0", "qiu", "jdk", 3000, false, false);
        //同步调用
//        DemoService demoService = client.create(DemoService.class);
//        String res = demoService.hello("qiuzhiq");
        
        //异步调用
        IAsyncObjectProxy demoService = client.createAsync(DemoService.class);
        RpcFuture future = demoService.call("hello", "qiuzhiq");
        Object res = future.get();

        LOGGER.info("返回的结果数据====>>>"+res);
        client.shutdown();
    }
}
