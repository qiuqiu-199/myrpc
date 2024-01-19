package io.qrpc.protocol.enumeration;

/**
 * @ClassName: RpcStatus
 * @Author: qiuzhiq
 * @Date: 2024/1/19 12:12
 * @Description: 服务调用状态，调用成功或者失败
 */

public enum  RpcStatus {
    SUCCESS(0),
    FAIL(1);

    private final int code;

    RpcStatus(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

}
