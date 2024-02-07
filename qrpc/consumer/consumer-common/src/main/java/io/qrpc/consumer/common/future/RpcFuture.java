package io.qrpc.consumer.common.future;


import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ResponseCache;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: RpcFuture
 * @Author: qiuzhiq
 * @Date: 2024/2/5 18:51
 * @Description: 基于CompletableFuture和AQS实现自定义的RpcFuture，用于实现异步转同步接收结果
 */

public class RpcFuture extends CompletableFuture<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFuture.class);

    private Sync sync;
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;  //开始时间，创建RpcFuture时的时间

    private long responseTimeThreshold = 5000; //默认超时时间


    //构造方法传入请求协议对象
    public RpcFuture(RpcProtocol<RpcRequest> protocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = protocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    //阻塞获取响应协议对象中的结果
    @Override
    public Object get() throws InterruptedException, ExecutionException {

        sync.acquire(-1); //TODO 待进一步理解

        return this.responseRpcProtocol != null ? this.responseRpcProtocol.getBody().getResult() : null;
    }

    //超时阻塞获取响应协议对象中的结果
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(responseTimeThreshold));
        if (success) {
            return this.responseRpcProtocol != null ? this.responseRpcProtocol.getBody().getResult() : null;
        } else {
            throw new RuntimeException("超时异常！请求id为" + this.requestRpcProtocol.getHeader().getRequestId() + ". 请求类名为：" + this.requestRpcProtocol.getBody().getClassName() + ". 请求方法名为：" + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    //暂时不支持，13章
    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    //暂时不支持，13章
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    //当消费者接收到提供者返回的结果时会调用这个方法，将响应结果存入RpcFuture中，这个时候会唤醒阻塞的线程获取响应结果
    // TODO 待进一步理解
    // 这个时候会唤醒阻塞的线程获取响应结果（冰河说的这里怎么理解？），13章
    public void done(RpcProtocol<RpcResponse> protocol) {
        this.responseRpcProtocol = protocol;

        sync.release(1);  //TODO 待进一步理解

        //TODO 待进一步理解
        //疑：为什么在这里检查响应时间？
        //答：因为是消费者接收到响应结果的时候才要检查
        //疑：在这里检查，如果超时了，但是又把结果放进来了，怎么处理超时收到的结果呢
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too long. The current requestId is " + protocol.getHeader().getRequestId() + ". Response time = " + responseTime + " ms");
        }

    }

    //使用AQS自定义实现同步器  TODO 待进一步理解
    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5677153504052936615L;

        //RpcFuture的两个状态  TODO 待进一步理解
        private final int done = 1;
        private final int pending = 0;

        //实现这两个方法来保证锁的独占性
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            getState();  //TODO 待进一步理解
            return getState() == done;
        }
    }
}
