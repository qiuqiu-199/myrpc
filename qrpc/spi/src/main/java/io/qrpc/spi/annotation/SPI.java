package io.qrpc.spi.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: SPI
 * @Author: qiuzhiq
 * @Date: 2024/2/27 14:14
 * @Description: 25章新增，用于标注SPI接口
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * 接口的默认实现类，这是扩展配置文件里的key，文件中key对应的value就是接口的默认实现类
     * 所以标记SPI注解的时候可以指定默认实现类
     */
    String value() default "";
}
