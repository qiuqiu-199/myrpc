package io.qrpc.protocol.base;

import java.io.Serializable;

/**
 * @ClassName: RpcMessage
 * @Author: qiuzhiq
 * @Date: 2024/1/17 16:47
 * @Description: 消息基础类
 */

public class RpcMessage implements Serializable {
    //是否单向发送
    private boolean oneway;
    //是否异步调用
    private boolean async;

    public boolean isOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
