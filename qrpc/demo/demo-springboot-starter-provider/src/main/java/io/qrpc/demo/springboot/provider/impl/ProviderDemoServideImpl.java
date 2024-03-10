package io.qrpc.demo.springboot.provider.impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: ProviderDemoServideImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/9 16:53
 * @Description:
 */

@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.qrpc.demo.api.DemoService",
        version = "1.0.0",
        group = "qiu",
        weight = 2
)
public class ProviderDemoServideImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
