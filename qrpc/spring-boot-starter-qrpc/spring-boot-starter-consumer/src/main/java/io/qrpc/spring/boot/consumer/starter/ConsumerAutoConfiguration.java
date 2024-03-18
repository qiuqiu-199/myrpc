package io.qrpc.spring.boot.consumer.starter;

import io.qrpc.consumer.RpcClient;
import io.qrpc.spring.boot.consumer.config.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @ClassName: ConsumerAutoConfiguration
 * @Author: qiuzhiq
 * @Date: 2024/3/9 12:00
 * @Description: 消费者端的自动配置类，自动根据yml配置文件配置参数类、根据参数类注入RpcClient组件
 */

//@Configuration
//@EnableConfigurationProperties  //开启自动配置功能
public class ConsumerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "qrpc.consumer")  //从配置文件中获取参数，value是前缀
    public ConsumerConfig consumerConfig(){
        return new ConsumerConfig();
    }

    @Bean
    public RpcClient rpcClient(final ConsumerConfig config){
        return new RpcClient(
                config.getVersion(),
                config.getGroup(),
                config.getRegistryType(),
                config.getRegistryAddr(),
                config.getRegistryLoadBalancer(),
                config.getSerializationType(),
                config.getProxyType(),
                config.isAync(),
                config.isOneway(),
                config.getTimeout(),
                config.getHeartbeatInterval(),
                config.getScanNotActiveChannelInterval(),
                config.getMaxRetryTimes(),
                config.getRetryInterval(),
                config.isEnableCacheResult(),
                config.getCacheResultExpire(),
                config.getReflectType(),
                config.getFallbackClassName(),
                config.isEnableRateLimiter(),
                config.getRateLimiterType(),
                config.getPermits(),
                config.getMilliSeconds(),
                config.getRateLimiterFailStrategy(),
                config.isEnableFusing(),
                config.getFusingStrategyType(),
                config.getTotalFailure(),
                config.getFusingMilliSeconds()
        );
    }
}
