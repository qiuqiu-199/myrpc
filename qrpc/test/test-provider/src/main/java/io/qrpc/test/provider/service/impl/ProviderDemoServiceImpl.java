package io.qrpc.test.provider.service.impl;

import io.qrpc.annotation.RpcService;
import io.qrpc.test.provider.service.DemoService;

/**
 * @ClassName: ProviderDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/1/17 11:55
 * @Description:
 */
@RpcService(interfaceClass = DemoService.class,
            interfaceClassName = "io.qrpc.test.provider.service.DemoService",
            version = "1.0.0",
            group = "qiu")
public class ProviderDemoServiceImpl implements DemoService {
}
