package io.test.consumer;

import io.qrpc.consumer.RpcClient;
import io.qrpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RpcConsumerTestNative
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:32
 * @Description:
 */

public class RpcConsumerTestNative {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerTestNative.class);

    public static void main(String[] args) {
        RpcClient client = new RpcClient("1.0.0", "qiu", "jdk", 3000, false, false);
        DemoService demoService = client.create(DemoService.class);
        String res = demoService.hello("qiuzhiq");
        LOGGER.info("返回的结果数据====>>>"+res);
        client.shutdown();
    }
}
