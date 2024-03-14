package io.qrpc.spring.annotation.consumer.Impl;

import io.qrpc.annotation.RpcReference;
import io.qrpc.demo.api.DemoService;
import io.qrpc.spring.annotation.consumer.ConsumerDemoService;
import io.qrpc.spring.annotation.consumer.fallback.FallbackDemoServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @ClassName: ConsumerDemoServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:27
 * @Description: 其他业务接口实现类
 */

@Service  //标注了该注解后，spring容器启动会扫描当前组件并注入容器中
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
            cacheResultExpire = 6000,
            reflectType = "jdk",
            fallbackClassName = "io.qrpc.spring.annotation.consumer.fallback.FallbackDemoServiceImpl",
            fallbackClass = FallbackDemoServiceImpl.class
    )
    private DemoService demoService;

    @Override
    public String otherBusiness(String name) {
        return demoService.sayHello(name);
    }
}
