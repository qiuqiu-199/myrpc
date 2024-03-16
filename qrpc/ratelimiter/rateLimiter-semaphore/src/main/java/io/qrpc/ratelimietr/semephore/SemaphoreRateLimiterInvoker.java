package io.qrpc.ratelimietr.semephore;

import io.qrpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * @ClassName: SemaphoreRateLimiterInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/15 15:16
 * @Description:
 */
@SpiClass
public class SemaphoreRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final static Logger log = LoggerFactory.getLogger(SemaphoreRateLimiterInvoker.class);

    private Semaphore semaphore;


    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public boolean tryAcquire() {
        log.warn("当前限流策略：semaphore...");
        return semaphore.tryAcquire();
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
