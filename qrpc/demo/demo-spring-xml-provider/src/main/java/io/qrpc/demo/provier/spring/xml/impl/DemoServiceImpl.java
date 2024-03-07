package io.qrpc.demo.provier.spring.xml.impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: DemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/6 11:01
 * @Description: 服务实现类
 */
@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.qrpc.demo.api.DemoService",
        version = "1.0.0",
        group = "qiu",
        weight = 2
)
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
