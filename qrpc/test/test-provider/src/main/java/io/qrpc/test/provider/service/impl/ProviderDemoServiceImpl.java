package io.qrpc.test.provider.service.impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: ProviderDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/1/17 11:55
 * @Description:
 */
@RpcService(interfaceClass = DemoService.class,
            interfaceClassName = "io.qrpc.test.api.DemoService",
            version = "1.0.0",
            group = "qiu")
public class ProviderDemoServiceImpl implements DemoService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        LOGGER.info("当前真实方法接收到的参数为====》{}",name );
        return "hello, " + name;
    }
}
