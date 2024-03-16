package io.qrpc.provider;

import io.qrpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.qrpc.provider.common.scanner.RpcServiceScanner;

/**
 * @ClassName: RpcSingleServer
 * @Author: qiuzhiq
 * @Date: 2024/1/17 11:00
 * @Description: RpcSingleServer类作为原生Java方式启动RPC框架，不依赖spring启动rpc框架的类
 */

public class RpcSingleServer extends BaseServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcSingleServer.class);

    /**
     * @author: qiu
     * @date: 2024/3/1 0:09
     * @description: 构造RpcServer，通过自定义扫描器扫描@RpcService注解通过反射生成对应的对象缓存在map中
     */
    public RpcSingleServer(
            String serverAddr,
            String scanPackage,
            String registryType,
            String registryAddr,
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
            int milliSeconds,
            String rateLimiterFailStrategy
    ) {
        //TODO 这里必须调用父类构造方法否则报错，原因不明
        super(
                serverAddr,
                registryType,
                registryAddr,
                registryLoadBalanceType,
                reflectType,
                heartbeatInterval,
                scanNotActiveChannelInterval,
                enableCacheResult,
                cacheResultExpire,
                maxConnectionCount,
                disuseStrategyType,
                enableRateLimiter,
                rateLimiterType,
                permits,
                milliSeconds,
                rateLimiterFailStrategy
        );

        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage,this.host,this.port,this.registryService);
        } catch (Exception e) {
            LOGGER.error("RPC Server init error",e);
        }
    }
}
