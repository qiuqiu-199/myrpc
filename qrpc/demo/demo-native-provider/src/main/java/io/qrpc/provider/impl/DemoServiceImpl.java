package io.qrpc.provider.impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.common.exception.RpcException;
import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: DemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/14 15:03
 * @Description:
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
        if ("fallback".equals(name)){
            throw new RpcException("触发异常关键词!");
        }
        return "hello" + name;
    }
}
