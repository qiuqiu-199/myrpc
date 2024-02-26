package io.qrpc.common.exception;

/**
 * @ClassName: RegistryException
 * @Author: qiuzhiq
 * @Date: 2024/2/25 14:33
 * @Description: 23章新增
 */

public class RegistryException extends RuntimeException{
    private static final long serialVersionUID = 3385472386492402328L;

    public RegistryException(final Throwable e){
        super(e);
    }

    public RegistryException(final String msg){
        super(msg);
    }

    public RegistryException(final String msg, final Throwable e){
        super(msg,e);
    }
}
