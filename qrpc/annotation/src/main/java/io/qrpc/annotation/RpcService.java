package io.qrpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @InterfaceName: RpcService
 * @Author: qiuzhiq
 * @Date: 2024/1/15 16:00
 * @Description: 自定义的服务提供者注解
 */

@Target({ElementType.TYPE})  //表示当前自定义的这个注解只能标注到类上
@Retention(RetentionPolicy.RUNTIME) //表示注解运行时会保留，从而可以通过反射获取注解信息
@Component  //当前注解要标注到服务接口的实现类上，也意味着实现类会被注入容器
public @interface RpcService {
    //接口的class和className二选一，前者指定类.class，后者指定类的全类名字符串
    /**
     * 接口的class
     */
    Class<?> interfaceClass() default void.class;
    /**
     * 接口的className
     */
    String interfaceClassName() default "";


    /**
     * 接口的版本
     */
    String version() default "1.0.0";
    /**
     * 接口的分组
     */
    String group() default "";


    /**
     * 服务权重，默认1
     */
    int weight() default 1;
}
