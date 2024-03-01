package io.test.consumer;

import io.qrpc.consumer.RpcClient;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.test.api.DemoService;
import org.junit.Before;
import org.junit.Test;
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

    //测试代理的同步异步获取结果用
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        RpcClient client = new RpcClient("127.0.0.1:2181","zookeeper","1.0.0", "qiu", "jdk", 3000333, false, false);
//        //同步调用
////        DemoService demoService = client.create(DemoService.class);
////        String res = demoService.hello("qiuzhiq");
//
//        //异步调用
//        IAsyncObjectProxy demoService = client.createAsync(DemoService.class);
//        RpcFuture future = demoService.call("hello", "qiuzhiq");
//        Object res = future.get();
//
//        LOGGER.info("返回的结果数据====>>>"+res);
//        client.shutdown();
    }

    //以下三个方法，整合RegistryService后的消费者端测试
    private RpcClient rpcClient;
    @Before
    public void init(){
        rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "qiu", "protostuff", "cglib","leastconnection",3000, false, false);
    }

    @Test
    public void testInterfaceRpc(){
        DemoService demoService = rpcClient.create(DemoService.class);
        String hello = demoService.hello("23章test create...。。。");
        LOGGER.info("提供者返回的数据====》》》"+hello);
        rpcClient.shutdown();
    }
    @Test
    public void testAsyncInterfaceRpc() throws ExecutionException, InterruptedException {
        IAsyncObjectProxy proxy = rpcClient.createAsync(DemoService.class);
        RpcFuture future = proxy.call("hello", "23章test createAsync...");
        LOGGER.info("提供者返回的数据====》》》"+future.get());
        rpcClient.shutdown();
    }
}
