package io.qrpc.consumer;

import io.qrpc.common.exception.RegistryException;
import io.qrpc.consumer.common.RpcConsumer;
import io.qrpc.proxy.api.ProxyFactory;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.config.ProxyConfig;
import io.qrpc.proxy.api.object.ObjectProxy;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.spi.loader.ExtensionLoader;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RpcClient
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:05
 * @Description:
 */

public class RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String serviceVersion;
    private String serviceGroup;
    private String seriliazationType;
    private String proxyType;
    private long timeout;
    private boolean async;
    private boolean oneway;
    private RegistryService registryService;

    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;

    private int maxRetryTimes = 3; //最大重试次数
    private int retryInterval = 1000;  //重试间隔时间

    //缓存相关变量
    private boolean enableCacheResult;
    private int cacheResultExpire;

    //服务容错-服务降级
    private String reflectType;
    private String fallbackClassName;
    @Setter  //通过set方法指定，因为xml方式的spring启动无法传递class
    private Class<?> fallbackClass;

    //服务容错-服务限流
    private boolean enableRateLimiter;
    private String rateLimiterType;
    private int permits;
    private int milliSeconds;
    private String rateLimiterFailStrategy;

    //构造方法
    public RpcClient(
            String serviceVersion,
            String serviceGroup,
            String registryType,
            String registryAddress,
            String registryLoadBalancer,
            String seriliazationType,
            String proxyType,
            boolean async,
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
            boolean enableRateLimiter,
            String rateLimiterType,
            int permits,
            int milliSeconds,
            String rateLimiterFailStrategy
    ) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.seriliazationType = seriliazationType;
        this.proxyType = proxyType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryType, registryAddress, registryLoadBalancer);
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.maxRetryTimes = maxRetryTimes;
        this.retryInterval = retryInterval;
        this.enableCacheResult = enableCacheResult;
        this.cacheResultExpire = cacheResultExpire;

        this.reflectType = reflectType;
        this.fallbackClassName = fallbackClassName;

        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
    }

    /**
     * @author: qiu
     * @date: 2024/2/25 14:28
     * @description: 根据服务中心地址、服务中心类型、负载均衡类型获取对应的服务中心
     */
    private RegistryService getRegistryService(String registryType, String registryAddress, String loadBalancer) {
        if (StringUtils.isEmpty(registryType))
            throw new IllegalArgumentException("未指定注册中心类型registryType！！！");

        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, loadBalancer));
        } catch (Exception e) {
            LOGGER.error("注册中心初始化registryService失败：{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 23:26
     * @description: 根据接口创建代理对象
     * 20章优化，通过ProxyFactory接收基于jdk实现的动态代理工厂对象，并初始化代理工厂对象，然后返回代理工厂对象创建的代理对象
     * 32章修改，使用SPI机制获取代理工厂接口的扩展类对象
     */
    public <T> T create(Class<T> interfaceClass) {
        LOGGER.info("RpcClient#create RPC客户端创建代理工厂并创建同步代理对象...");
        //这里多传入一个Consumer对象

        //使用ProxyFactory接口接收代理工厂对象在一定程度上具备了扩展性，为后续SPI技术打下基础
        //32章SPI扩展代理方式，这里使用SPI加载扩展类
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxyType);
        proxyFactory.init(  //初始化生成ObjectProxy对象
                new ProxyConfig<>(
                        interfaceClass,
                        serviceVersion,
                        serviceGroup,
                        seriliazationType,
                        timeout,
                        async,
                        oneway,
                        RpcConsumer.getInstance(
                                this.heartbeatInterval,
                                this.scanNotActiveChannelInterval,
                                this.maxRetryTimes,
                                this.retryInterval
                        ),
                        registryService,
                        enableCacheResult,
                        cacheResultExpire,
                        reflectType,
                        fallbackClassName,
                        fallbackClass,
                        enableRateLimiter,
                        rateLimiterType,
                        permits,
                        milliSeconds,
                        rateLimiterFailStrategy

                )
        );
        return proxyFactory.getProxy(interfaceClass);
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 16:42
     * @description: 19章，构建异步化调用代理对象
     */
    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        LOGGER.info("RpcClient#createAsync RPC客户端创建异步化代理对象...");
        return new ObjectProxy<>(
                interfaceClass,
                serviceVersion,
                serviceGroup,
                seriliazationType,
                timeout,
                RpcConsumer.getInstance(
                        this.heartbeatInterval,
                        this.scanNotActiveChannelInterval,
                        this.maxRetryTimes,
                        this.retryInterval
                ),
                async,
                oneway,
                registryService,
                enableCacheResult,
                cacheResultExpire,
                reflectType,
                fallbackClassName,
                fallbackClass,
                enableRateLimiter,
                rateLimiterType,
                permits,
                milliSeconds,
                rateLimiterFailStrategy
        );
    }

    public void shutdown() {
        LOGGER.info("RpcClient#shutdown...");
        RpcConsumer.getInstance(
                this.heartbeatInterval,
                this.scanNotActiveChannelInterval,
                this.maxRetryTimes,
                this.retryInterval
        ).close();
    }
}
