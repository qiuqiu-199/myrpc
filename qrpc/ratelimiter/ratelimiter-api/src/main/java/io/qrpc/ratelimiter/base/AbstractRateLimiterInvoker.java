package io.qrpc.ratelimiter.base;

import io.qrpc.ratelimiter.api.RateLimiterInvoker;

/**
 * @ClassName: AbstractRateLimiterInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/14 21:00
 * @Description: 基础限流器实现类
 */

public abstract class AbstractRateLimiterInvoker implements RateLimiterInvoker {
    protected int permits;
    protected int milliSeconds;

    /**
     * @author: qiu
     * @date: 2024/3/14 21:12
     * @description: 初始化，获取限流上限与执行周期
     */
    @Override
    public void init(int permits, int milliSeconds) {
        this.permits = permits;
        this.milliSeconds = milliSeconds;
    }
}
