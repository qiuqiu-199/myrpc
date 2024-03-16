package io.qrpc.ratelimiter.guava;

import com.google.common.util.concurrent.RateLimiter;
import io.qrpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: GuavaRateLimiterInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/15 15:25
 * @Description:
 */
@SpiClass
public class GuavaRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final static Logger log = LoggerFactory.getLogger(GuavaRateLimiterInvoker.class);
    private RateLimiter rateLimiter;

    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);

        //每秒最多允许的个数
        double permitsPerSeconds = ((double)permits) / milliSeconds *1000;
        this.rateLimiter = RateLimiter.create(permitsPerSeconds);
    }

    @Override
    public boolean tryAcquire() {
        log.warn("当前限流策略：guava...");
        return rateLimiter.tryAcquire();
    }

    @Override
    public void release() {
        //使用guava库获取资源时不需要手动释放，所以这里为空
    }
}
