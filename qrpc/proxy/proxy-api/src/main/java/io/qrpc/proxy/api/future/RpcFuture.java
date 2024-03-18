package io.qrpc.proxy.api.future;


import io.qrpc.common.threadPool.ClientThreadPool;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcStatus;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import io.qrpc.proxy.api.callback.AsyncRpcCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: RpcFuture
 * @Author: qiuzhiq
 * @Date: 2024/2/5 18:51
 * @Description: 基于CompletableFuture和AQS实现自定义的RpcFuture，用于实现异步转同步接收结果
 */

public class RpcFuture extends CompletableFuture<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFuture.class);

    private Sync sync;  //基于AQS实现的内部类
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;  //开始时间，创建RpcFuture时的时间

    //3.5节新增回调接口的list和锁lock
    private List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();//存放回调接口
    private ReentrantLock lock = new ReentrantLock();//用于添加和执行回调方法时加锁和解锁

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

        return this.getResult(this.responseRpcProtocol);
    }

    //超时阻塞获取响应协议对象中的结果
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        //17章之前下面这一行存在错误，responseTimeThreshold应修改为timeout
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            return this.getResult(this.responseRpcProtocol);
        } else {
            LOGGER.error("超时时间设定为：===" + timeout);
            throw new RuntimeException("超时异常！请求id为" + this.requestRpcProtocol.getHeader().getRequestId() + ". 请求类名为：" + this.requestRpcProtocol.getBody().getClassName() + ". 请求方法名为：" + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/14 15:48
     * @description: 13节，根据调用结果不同返回不同结果，调用成功返回响应体，否则抛异常，由容错层处理
     */
    private Object getResult(RpcProtocol<RpcResponse> responseProtocol) {
        if (responseProtocol == null)
            return null;

        if (responseProtocol.getHeader().getStatus() == RpcStatus.FAIL.getCode()) {
            throw new RuntimeException("服务调用失败，异常信息：" + responseProtocol.getBody().getError());
        }

        return responseProtocol.getBody().getResult();
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
        LOGGER.info("Rpcfuture#done...");
        this.responseRpcProtocol = protocol;

        sync.release(1);  //TODO 待进一步理解

        //3.5节新增，唤醒线程时调用invokeCallback方法，以便消费者接收到数据后立即执行回调方法 TODO 待进一步理解
        invokeCallbacks();

        //TODO 待进一步理解
        //疑：为什么在这里检查响应时间？
        //答：因为是消费者接收到响应结果的时候才要检查
        //疑：在这里检查，如果超时了，但是又把结果放进来了，怎么处理超时收到的结果呢
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too long. The current requestId is " + protocol.getHeader().getRequestId() + ". Response time = " + responseTime + " ms");
        }
    }


    //3.5增加回调相关方法
    //添加回调方法，接收future的时候调用
    public RpcFuture addCallback(AsyncRpcCallback callback) {
        LOGGER.info("RpcFuture#addCallback...");
        lock.lock();
        try {
            if (isDone()) { //如果接收到了处理结果就执行回调方法，否则将回调方法加入list中等待
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    //执行回调方法，用于异步执行回调方法
    private void runCallback(final AsyncRpcCallback callback) {
        LOGGER.info("RpcFuture#runCallback...");
        final RpcResponse rpcResponseBody = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(() -> {
            if (!rpcResponseBody.isError()) {
                callback.onSuccess(rpcResponseBody.getResult());
            } else {
                callback.onException(new RuntimeException("Response error!", new Throwable(rpcResponseBody.getError())));
            }
        });
    }

    //处理list中的callback
    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    //使用AQS自定义实现同步器  TODO 待进一步理解
    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5677153504052936615L;

        //RpcFuture的两个状态  TODO 待进一步理解
        private final int done = 1;
        private final int pending = 0;

        //实现这两个方法来保证锁的独占性
        protected boolean tryAcquire(int acquires) {
            LOGGER.info("RpcFuture.Sync#tryAcquire...");
            return getState() == done;
        }

        protected boolean tryRelease(int arg) {
            LOGGER.info("RpcFuture.Sync#tryRelease...");
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            LOGGER.info("RpcFuture.Sync#isDone...");
            getState();  //TODO 待进一步理解
            return getState() == done;
        }
    }
}
