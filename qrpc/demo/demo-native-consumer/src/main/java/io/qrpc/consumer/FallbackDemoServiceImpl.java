package io.qrpc.consumer;

import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: FallbackDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/14 14:54
 * @Description: 服务容错类
 */

public class FallbackDemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "调用异常，容错处理类正在处理中，参数为name = "+name;
    }
}
