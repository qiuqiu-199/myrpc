package io.qrpc.annotation;

import org.springframework.beans.factory.annotation.Autowired;

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

@Target({ElementType.FIELD})  //标注到字段上，详见9章以注解形式启动RPC框架内容
@Retention(RetentionPolicy.RUNTIME)  //表示注解运行时会保留，从而可以通过反射获取注解信息
@Autowired //表示标注了RpcReference注解的字段上会进行自动装配
public @interface RpcReference {
    //版本号与分组
    String version() default "1.0.0";
    String group() default "";

    //注册中心类型，默认zookeeper，其他类型包括：nacos、etcd和consul等
    String registryType() default "zookeeper";
    //注册中心的地址
    String registryAddress() default "127.0.0.1:2181";
    //注册中心的负载均衡策略，默认基于zookeeper的一致性哈希
    String registryLoadbalanceType() default "zkconsistencehash";

    //序列化类型，默认protostuff，其他：kryo、json、jdk、hessian2、fst
    String serializationType() default "protostuff";

    //代理方式，默认jdk，其他：javassist、cglib
    String proxyType() default "jdk";

    //是否异步调用，默认否
    boolean async() default false;
    //是否单向调用，默认否
    boolean oneway() default false;

    //超时时间，默认5秒
    long timeout() default 5000;

    //心跳相关，默认心跳间隔时间3秒，扫描间隔时间60秒
    int heartbeatInterval() default 3000;
    int scanNotActiveChannelInterval() default 60000;

    //重试机制，最大重试次数默认3，重试间隔时间默认3秒
    int maxRetryTimes() default 3;
    int retryInterval() default 3000;

    //缓存层，默认开启缓存，缓存有效时间5秒
    boolean enableCacheResult() default true;
    int cacheResultExpire() default 5000;

    //服务容错-服务降级
    String reflectType() default "jdk";
    String fallbackClassName() default "void.class";
    Class<?> fallbackClass() default void.class;

    //服务容错-服务限流
    boolean enableRateLimiter() default true;
    String rateLimiterType() default "counter";
    int permits() default 100;
    int milliSeconds() default 1000;
    String rateLimiterFailStrategy() default "exception";

}
