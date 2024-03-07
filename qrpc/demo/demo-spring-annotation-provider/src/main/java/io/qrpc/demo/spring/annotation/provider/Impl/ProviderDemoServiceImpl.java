package io.qrpc.demo.spring.annotation.provider.Impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: ProviderDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:19
 * @Description:
 */

@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.qrpc.demo.api.DemoService",
        version = "1.0.0",
        group = "qiu",
        weight = 2
)
public class ProviderDemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
