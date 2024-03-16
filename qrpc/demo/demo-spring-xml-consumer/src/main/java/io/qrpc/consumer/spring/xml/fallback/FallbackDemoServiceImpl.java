package io.qrpc.consumer.spring.xml.fallback;

import io.qrpc.demo.api.DemoService;

/**
 * @ClassName: FallbackDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/14 23:33
 * @Description:
 */

public class FallbackDemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "fallback hello "+name+", wo are dealing with the thing...";
    }
}
