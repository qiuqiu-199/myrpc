package io.qrpc.demo.springboot.consumer.service.impl;

import io.qrpc.annotation.RpcReference;
import io.qrpc.demo.api.DemoService;
import io.qrpc.demo.springboot.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

/**
 * @ClassName: ConsumerDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/9 20:05
 * @Description:
 */
@Service
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    //这里通过RpcReference注解自动注入属性，注入逻辑过程在后置处理器RpcConsumerPostProcessor中
    @RpcReference(
            version = "1.0.0",
            group = "qiu",
            registryType = "nacos",
            registryAddress = "127.0.0.1:8848",
            registryLoadbalanceType = "random",
            serializationType = "protostuff",
            proxyType = "cglib",
            async = false,
            oneway = false,
            timeout = 5000,
            heartbeatInterval = 3000,
            scanNotActiveChannelInterval = 60000,
            maxRetryTimes = 3,
            retryInterval = 3000,
            enableCacheResult = true,
            cacheResultExpire = 6000
    )
    private DemoService demoService;  //这里的红波浪线不用管
    @Override
    public String otherBusiness(String str) {
        return demoService.sayHello(str);
    }
}
