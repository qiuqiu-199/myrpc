package io.qrpc.fusing.base;

import io.qrpc.constants.RpcConstants;
import io.qrpc.fusing.api.FusingInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: AbstractFusingInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/16 16:16
 * @Description:
 */

public abstract class AbstractFusingInvoker implements FusingInvoker {

    private final static Logger log = LoggerFactory.getLogger(AbstractFusingInvoker.class);

    /**
     * 熔断状态：1-关闭，2-半开启，3-开启
     */
    protected final AtomicInteger fusingStatus = new AtomicInteger(RpcConstants.FUSING_STATUS_CLOSED);

    //当前调用次数与当前调用失败次数
    protected AtomicInteger curCounter = new AtomicInteger(0);
    protected AtomicInteger curCounterFailure = new AtomicInteger(0);

    //熔断开启时，熔断时间的开始时间点
    protected volatile long lastTimeStamp = System.currentTimeMillis();

    //熔断阈值，可以是失败次数，可以是失败率
    protected double totalFailure;
    //熔断周期
    protected int milliSeconds;

    //重置次数
    protected void resetCounter(){
        curCounter.set(0);
        curCounterFailure.set(0);
    }

    @Override
    public void incrementCount() {
        curCounter.incrementAndGet();
    }

    @Override
    public void incrementFailureCount() {
        curCounterFailure.incrementAndGet();
    }

    @Override
    public void init(int totalFailure, int milliSeconds) {
        this.totalFailure = totalFailure > 0 ? totalFailure : RpcConstants.FUSING_TOTALFAILURE_DEFAULT;
        this.milliSeconds = milliSeconds > 0 ? milliSeconds : RpcConstants.FUSING_MILLISECONDS_DEFAULT;
    }
}
