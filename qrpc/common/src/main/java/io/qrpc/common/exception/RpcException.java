package io.qrpc.common.exception;

/**
 * @ClassName: RpcException
 * @Author: qiuzhiq
 * @Date: 2024/3/14 15:06
 * @Description:
 */

public class RpcException extends RuntimeException {
    private static final long serialVersionUID = -4547073109636080836L;
    public RpcException(final Throwable e){
        super(e);
    }

    public RpcException(final String msg){
        super(msg);
    }

    public RpcException(final String msg, final Throwable e){
        super(msg,e);
    }
}
