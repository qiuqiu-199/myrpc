package io.qrpc.test.scanner.provider;

import io.qrpc.annotation.RpcService;
import io.qrpc.test.scanner.service.DemoService;

/**
 * @ClassName: ProviderDemoService
 * @Author: qiuzhiq
 * @Date: 2024/1/16 15:47
 * @Description: 服务的实现类
 */
@RpcService(interfaceClass = DemoService.class,interfaceClassName = "io.qrpc.test.scanner.service.DemoService",version = "1.0.0",group = "qiu")
public class ProviderDemoService implements DemoService {
}
