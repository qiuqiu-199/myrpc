package io.qrpc.spi.annotation;

import java.lang.annotation.*;

/**
 * @author: qiu
 * @date: 2024/2/28 13:42
 * @description: 25章新增，用于标注SPI实现类
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SpiClass {
}
