package io.qrpc.proxy.jdk;

import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.proxy.api.object.ObjectProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @ClassName: JdkProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:16
 * @Description:
 */

public class JdkProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdkProxyFactory.class);

    private String serviceVersion;
    private String serviceGroup;
    private String seriliazationType;
    private long timeout;
    private Consumer consumer;//消费者启动端
    private boolean async;
    private boolean oneway;

    public JdkProxyFactory(String serviceVersion, String serviceGroup, String seriliazationType, long timeout, Consumer consumer, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.seriliazationType = seriliazationType;
        this.timeout = timeout;
        this.consumer = consumer;
        this.async = async;
        this.oneway = oneway;
    }

    public <T> T getProxy(Class<T> clazz){
        LOGGER.info("JdkProxyFactory#getProxy...");
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz}, //new Class<?>[]{clazz}
                new ObjectProxy<T>(clazz,serviceVersion,serviceGroup,timeout,consumer,seriliazationType,async,oneway)
                );
    }
}
