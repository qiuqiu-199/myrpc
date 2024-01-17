package io.qrpc.protocol.response;

import io.qrpc.protocol.base.RpcMessage;

/**
 * @ClassName: RpcResponse
 * @Author: qiuzhiq
 * @Date: 2024/1/17 16:59
 * @Description: Rpc响应消息的封装类，对应的请求id在消息头中
 */

public class RpcResponse extends RpcMessage {
    private static final long serialVersionUID = -7274511319175271486L;

    private String error;

    private Object result;

    public boolean isError(){return error != null;}


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
