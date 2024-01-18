package io.qrpc.common.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName: SerializerException
 * @Author: qiuzhiq
 * @Date: 2024/1/18 9:37
 * @Description: 序列化异常类，提供三种构造方法
 */

public class SerializerException extends RuntimeException {
    private static final long serialVersionUID = 9024776108737357811L;

    public SerializerException(final Throwable e) {
        super(e);
    }

    public SerializerException(final String message) {
        super(message);
    }

    public SerializerException(final String message, final Throwable e) {
        super(message, e);
    }
}
