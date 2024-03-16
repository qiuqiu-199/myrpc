package io.qrpc.ratelimiter.counter;

import io.qrpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: CounterRateLimiterInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/14 21:03
 * @Description: 基于计数器实现的限流策略-固定窗口算法
 */
@SpiClass
public class CounterRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final static Logger log = LoggerFactory.getLogger(CounterRateLimiterInvoker.class);

    //记录当前执行周期内请求数
    private AtomicInteger count = new AtomicInteger(0);
    //记录上次周期开始时间，用于判断执行周期分界点
    private volatile long lastTimeStamp = System.currentTimeMillis();
//    //保存当前请求线程是否得到许可，是否可以由服务端处理该请求
    private final ThreadLocal<Boolean> threadLocal = new ThreadLocal<>();

    /**
     * @author: qiu
     * @date: 2024/3/14 21:17
     * @description: 获取许可。开启新的周期，或者，执行周期内请求处理数未达到上限可以得到许可，继续处理新的请求，否则未得到许可。
     */
    @Override
    public boolean tryAcquire() {
        log.warn("当前限流策略：计数器...");
        long curTimeStamp = System.currentTimeMillis();
        log.warn("当前count={}...",count);

        //新的执行周期开始时，重置计数器
        if (curTimeStamp - lastTimeStamp >= milliSeconds){
            log.error("条件1：curTimeStamp - lastTimeStamp = {}",curTimeStamp - lastTimeStamp);
//            lastTimeStamp = curTimeStamp;
            count.set(0);
//            threadLocal.set(true);  //bug源头，详细见13节bug1
            return true;
        }

        //执行周期内，请求数小于上限可以继续执行
        if (count.incrementAndGet() <= permits){
            log.error("条件2：curTimeStamp - lastTimeStamp = {}",curTimeStamp - lastTimeStamp);
//            threadLocal.set(true);
            return true;
        }

        return false;
    }

    /**
     * @author: qiu
     * @date: 2024/3/14 21:18
     * @description: 释放资源，当前请求线程得到了许可才能释放资源。
     * TODO
     */
    @Override
    public void release() {
//        if (threadLocal.get()){
//            try {
//                count.decrementAndGet();
//            }finally {
//                threadLocal.remove();
//            }
//        }
    }
}
