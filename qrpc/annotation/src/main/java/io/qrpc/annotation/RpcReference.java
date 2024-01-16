package io.qrpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @InterfaceName: RpcService
 * @Author: qiuzhiq
 * @Date: 2024/1/15 16:00
 * @Description: 自定义的服务消费者注解
 */

@Target({ElementType.FIELD})  //标注到字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    //版本号与分组
    String version() default "1.0.0";
    String group() default "";

    //注册中心类型，默认zookeeper，其他类型包括：nacos、etcd和consul等
    String registryType() default "zookeeper";
    //注册中心的地址
    String registryAddress() default "127.0.0.1:2181";
    //注册中心的负载均衡策略，默认基于zookeeper的一致性哈希
    String loadBalanceType() default "zkconsistencehash";

    //序列化类型，默认protostuff，其他：kryo、json、jdk、hessian2、fst
    String serializationType() default "protostuff";
    //超时时间，默认5秒
    long timeout() default 5000;
    //是否异步调用，默认否
    boolean async() default false;
    //是否单向调用，默认否
    boolean oneway() default false;
    //代理方式，默认jdk，其他：javassist、cglib
    String proxy() default "jdk";
}
