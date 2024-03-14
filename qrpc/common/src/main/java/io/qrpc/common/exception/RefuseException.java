package io.qrpc.common.exception;

/**
 * @ClassName: RefuseException
 * @Author: qiuzhiq
 * @Date: 2024/3/13 12:43
 * @Description:
 */

public class RefuseException extends RuntimeException {
    private static final long serialVersionUID = 4210841145205138600L;
    public RefuseException(final Throwable e){
        super(e);
    }

    public RefuseException(final String msg){
        super(msg);
    }

    public RefuseException(final String msg, final Throwable e){
        super(msg,e);
    }
}
