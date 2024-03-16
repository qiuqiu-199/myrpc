package io.qrpc.spring.boot.consumer.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ConsumerConfig
 * @Author: qiuzhiq
 * @Date: 2024/3/9 11:58
 * @Description: 参数类，在自动配置类中通过yml配置文件获取参数，并注入容器中
 */
@Data
@NoArgsConstructor
public class ConsumerConfig {
    private String version;
    private String group;

    private String registryType;
    private String registryAddr;
    private String registryLoadBalancer;

    private String serializationType;
    private String proxyType;
    private boolean aync;
    private boolean oneway;

    private long timeout;
    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;
    private int maxRetryTimes;
    private int retryInterval;

    private boolean enableCacheResult;
    private int cacheResultExpire;

    //服务容错-服务降级
    private String reflectType;
    private String fallbackClassName;
    private Class<?> fallbackClass;

    //服务容错-服务限流
    private boolean enableRateLimiter;
    private String rateLimiterType;
    private int permits;
    private int milliSeconds;
    private String rateLimiterFailStrategy;

    public ConsumerConfig(
            String version,
            String group,
            String registryType,
            String registryAddr,
            String registryLoadBalancer,
            String serializationType,
            String proxyType,
            boolean aync,
            boolean oneway,
            long timeout,
            int heartbeatInterval,
            int scanNotActiveChannelInterval,
            int maxRetryTimes,
            int retryInterval,
            boolean enableCacheResult,
            int cacheResultExpire,
            String reflectType,
            String fallbackClassName,
            Class<?> fallbackClass,
            boolean enableRateLimiter,
            String rateLimiterType,
            int permits,
            int milliSeconds,
            String rateLimiterFailStrategy
    ) {
        this.version = version;
        this.group = group;
        this.registryType = registryType;
        this.registryAddr = registryAddr;
        this.registryLoadBalancer = registryLoadBalancer;
        this.serializationType = serializationType;
        this.proxyType = proxyType;
        this.aync = aync;
        this.oneway = oneway;
        this.timeout = timeout;
        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.maxRetryTimes = maxRetryTimes;
        this.retryInterval = retryInterval;
        this.enableCacheResult = enableCacheResult;
        this.cacheResultExpire = cacheResultExpire;

        this.reflectType = reflectType;
        this.fallbackClassName = fallbackClassName;
        this.fallbackClass = fallbackClass;

        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
    }
}
