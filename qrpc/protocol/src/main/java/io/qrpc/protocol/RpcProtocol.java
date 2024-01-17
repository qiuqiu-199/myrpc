package io.qrpc.protocol;

import io.qrpc.protocol.header.RpcHeader;

import java.io.Serializable;

/**
 * @ClassName: RpcProtocol
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:21
 * @Description: 协议
 */

public class RpcProtocol<T> implements Serializable {
    private static final long serialVersionUID = 6513792687859478924L;

    private RpcHeader header;
    private T body;

    public RpcHeader getHeader() {
        return header;
    }

    public void setHeader(RpcHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
