package io.qrpc.proxy.api.config;

import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.registry.api.RegistryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: ProxyConfig
 * @Author: qiuzhiq
 * @Date: 2024/2/21 22:46
 * @Description: 20章新增，用于配置动态代理
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyConfig<T> implements Serializable {
    private static final long serialVersionUID = 2447008678604797411L;
    private Class<T> clazz;
    private String serviceVersion;
    private String serviceGroup;
    private String serializationType;
    private long timeout;
    private boolean async;
    private boolean oneway;
    private Consumer consumer;
    private RegistryService registryService;

    private boolean enableCacheResult;
    private int cacheResultExpire;

    private String reflectType;
    private String fallbackClassName;
    private Class<?> fallbackClass;

    //服务容错-服务限流
    private boolean enableRateLimiter;
    private String rateLimiterType;
    private int permits;
    private int milliSeconds;
    private String rateLimiterFailStrategy;
    //服务容错-服务熔断
    private boolean enableFusing;
    private String fusingStrategyType;
    private int totalFailure;
    private int fusingMilliSeconds;
}
