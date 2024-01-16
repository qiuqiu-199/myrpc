package io.qrpc.test.scanner.consumer.service.impl;

import io.qrpc.annotation.RpcReference;
import io.qrpc.test.scanner.consumer.service.ConsumerBusinessSevice;
import io.qrpc.test.scanner.service.DemoService;

/**
 * @ClassName: ConsumerBusinessServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/1/16 15:53
 * @Description:
 */

public class ConsumerBusinessServiceImpl implements ConsumerBusinessSevice {
    @RpcReference(registryType = "zookeeper",registryAddress = "127.0.0.1:2181",version = "1.0.0",group = "qiu")
    private DemoService demoService;
}
