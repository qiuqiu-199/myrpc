package io.qrpc.spring.boot.provider.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProviderConfig
 * @Author: qiuzhiq
 * @Date: 2024/3/9 11:31
 * @Description: 参数类，在自动配置类中通过yml配置文件获取参数，并注入容器中
 */
@Data
@NoArgsConstructor
public class ProviderConfig {

    private String serverAddr;

    private String registryType;
    private String registryAddr;
    private String registryLoadBalanceType;

    private String reflectType;

    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;

    private boolean enableCacheResult;
    private int cacheResultExpire;

    private int maxConnectionCount;
    private String disuseStrategyType;

    //服务容错-服务限流
    private boolean enableRateLimiter;
    private String rateLimiterType;
    private int permits;
    private int rateLimiterMilliSeconds;
    private String rateLimiterFailStrategy;
    //服务容错-服务熔断
    private boolean enableFusing;
    private String fusingStrategyType;
    private int totalFailure;
    private int fusingMilliSeconds;

    public ProviderConfig(String serverAddress,
                          String registryType,
                          String registryAddress,
                          String registryLoadBalanceType,
                          String reflectType,
                          int heartbeatInterval,
                          int scanNotActiveChannelInterval,
                          boolean enableCacheResult,
                          int cacheResultExpire,
                          int maxConnectionCount,
                          String disuseStrategyType,
                          boolean enableRateLimiter,
                          String rateLimiterType,
                          int permits,
                          int rateLimiterMilliSeconds,
                          String rateLimiterFailStrategy,
                          boolean enableFusing,
                          String fusingStrategyType,
                          int totalFailure,
                          int fusingMilliSeconds
    ) {
        this.serverAddr = serverAddress;

        this.registryType = registryType;
        this.registryAddr = registryAddress;
        this.registryLoadBalanceType = registryLoadBalanceType;

        this.reflectType = reflectType;

        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;

        if (cacheResultExpire > 0)
            this.cacheResultExpire = cacheResultExpire;
        this.enableCacheResult = enableCacheResult;

        this.maxConnectionCount = maxConnectionCount;
        this.disuseStrategyType = disuseStrategyType;

        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.rateLimiterMilliSeconds = rateLimiterMilliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;

        this.enableFusing = enableFusing;
        this.fusingStrategyType = fusingStrategyType;
        this.totalFailure = totalFailure;
        this.fusingMilliSeconds = fusingMilliSeconds;
    }
}
