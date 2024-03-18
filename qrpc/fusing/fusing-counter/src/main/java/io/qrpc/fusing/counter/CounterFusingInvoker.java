package io.qrpc.fusing.counter;

import io.qrpc.constants.RpcConstants;
import io.qrpc.fusing.base.AbstractFusingInvoker;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: CounterFusingInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/16 16:42
 * @Description: 基于计数器实现熔断器
 */
@SpiClass
public class CounterFusingInvoker extends AbstractFusingInvoker {
    private final static Logger log = LoggerFactory.getLogger(CounterFusingInvoker.class);

    /**
     * @author: qiu
     * @date: 2024/3/16 16:59
     * @description: 基于计数器实现的熔断规则
     */
    @Override
    public boolean invokeFusingStrategy() {
        log.warn("当前熔断规则：计数器...");

        /**
         * @author: qiu
         * @date: 2024/3/16 17:00
         * @description: 根据当前处于熔断器何种状态进入不同的状态处理
         * -返回true表示需要进行服务熔断
         * -返回false表示不需要
         */
        switch (fusingStatus.get()) {
            case RpcConstants.FUSING_STATUS_CLOSED:
                return this.invokeClosedFusingStrategy();
            case RpcConstants.FUSING_STATUS_HALF_OPEN:
                return this.invokeHalfOpenFusingStrategy();
            case RpcConstants.FUSING_STATUS_OPEN:
                return this.invokeOpenFusingStrategy();
        }
        return this.invokeOpenFusingStrategy();
    }

    /**
     * @author: qiu
     * @date: 2024/3/16 17:01
     * @description: 关闭状态下的处理方式
     * -如果进入了新的熔断周期，重置调用次数；
     * -否则，熔断周期内如果失败次数达到阈值，进入半熔断状态，返回true，需要进行服务熔断，否则返回false
     */
    private boolean invokeClosedFusingStrategy() {
        long curTimeStamp = System.currentTimeMillis();

        if (curTimeStamp - lastTimeStamp >= milliSeconds) {
            lastTimeStamp = curTimeStamp;
            this.resetCounter();
            return false;
        }

        if (curCounterFailure.get() >= totalFailure) {
            lastTimeStamp = curTimeStamp;
            fusingStatus.set(RpcConstants.FUSING_STATUS_OPEN);
            return true;
        }

        return false;
    }

    /**
     * @author: qiu
     * @date: 2024/3/16 17:01
     * @description: 半熔断状态下的处理方式
     * -如果失败次数不大于零，说明服务能力恢复了，此时熔断器进入关闭状态并重置次数，返回false
     * -否则，服务能力没有恢复，进入熔断状态，返回true，需要进行服务熔断
     */
    private boolean invokeHalfOpenFusingStrategy() {
        long curTimeStamp = System.currentTimeMillis();

        if (curCounterFailure.get() <= 0) {
            fusingStatus.set(RpcConstants.FUSING_STATUS_CLOSED);
            lastTimeStamp = curTimeStamp;
            this.resetCounter();
            return false;
        }

        fusingStatus.set(RpcConstants.FUSING_STATUS_OPEN);
        lastTimeStamp = curTimeStamp;
        return true;
    }

    /**
     * @author: qiu
     * @date: 2024/3/16 17:01
     * @description: 熔断状态下的处理方式
     * -如果进入了新的熔断周期，熔断器进入关闭状态，重置调用次数；
     * -否则，返回true，需要进行服务熔断
     */
    private boolean invokeOpenFusingStrategy() {
        long curTimeStamp = System.currentTimeMillis();

        if (curTimeStamp - lastTimeStamp >= milliSeconds) {
            fusingStatus.set(RpcConstants.FUSING_STATUS_HALF_OPEN);
            lastTimeStamp = curTimeStamp;
            this.resetCounter();
            return false;
        }

        return true;
    }
}
