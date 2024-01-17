package io.qrpc.common.scanner.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: IdFactory
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:12
 * @Description:
 */

public class IdFactory {
    private final static AtomicLong REQUEST_ID_LEN=new AtomicLong(0);
    public static Long getId(){return REQUEST_ID_LEN.incrementAndGet();}
}
