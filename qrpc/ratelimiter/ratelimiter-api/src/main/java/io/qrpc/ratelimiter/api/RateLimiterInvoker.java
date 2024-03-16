package io.qrpc.ratelimiter.api;

import io.qrpc.constants.RpcConstants;
import io.qrpc.spi.annotation.SPI;

/**
 * @InterfaceName: RateLimiterInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/14 20:56
 * @Description: 限流器接口
 */
@SPI(value = RpcConstants.RATE_LIMITER_DEFAULT)
public interface RateLimiterInvoker {
    /**
     * @author: qiu
     * @date: 2024/3/14 21:15
     * @return: boolean-获取资源，成功为true。这里指请求可以被服务端处理
     * @description: 获取资源
     */
    boolean tryAcquire();
    /**
     * @author: qiu
     * @date: 2024/3/14 21:16
     * @description: 释放资源
     */
    void release();
    /**
     * @author: qiu
     * @date: 2024/3/14 21:12
     * @param: permits-执行周期内的资源上限，这里指执行周期内可以处理的请求数量上限
     * @param: milliSeconds-执行周期，单位毫秒
     * @description:
     */
    default void init(int permits, int milliSeconds){}
}
