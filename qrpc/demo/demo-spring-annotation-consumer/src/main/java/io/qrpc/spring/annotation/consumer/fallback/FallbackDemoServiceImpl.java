package io.qrpc.spring.annotation.consumer.fallback;

import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: FallbackDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/14 14:54
 * @Description:
 */

public class FallbackDemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "fallback hello "+name+", wo are dealing with the thing...";
    }
}
