package io.qrpc.consumer.common.context;

import io.qrpc.consumer.common.future.RpcFuture;

/**
 * @ClassName: RpcContext
 * @Author: qiuzhiq
 * @Date: 2024/2/7 19:56
 * @Description: RPC的上下文
 */

public class RpcContext {
    private RpcContext(){}

    //单例模式
    private static final RpcContext AGENT = new RpcContext();

    //使用InheritableThreadLocal保存线程上下文
    private static InheritableThreadLocal<RpcFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    //获取RPC上下文
    public static RpcContext getContext(){
        return AGENT;
    }

    //存放RpcFuture
    public void setFuture(RpcFuture future){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(future);
    }

    //获取RpcFuture
    public RpcFuture getFuture(){
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    //移除RpcFuture
    public void removeFuture(){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
