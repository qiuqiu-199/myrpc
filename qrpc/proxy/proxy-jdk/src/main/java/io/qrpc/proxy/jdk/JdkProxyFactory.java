package io.qrpc.proxy.jdk;

import io.qrpc.proxy.api.BaseProxyFactory;
import io.qrpc.proxy.api.ProxyFactory;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @ClassName: JdkProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:16
 * @Description: 20章动态代理扩展优化后的简洁版本
 */
@SpiClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdkProxyFactory.class);

    /**
     * @author: qiu
     * @date: 2024/2/21 23:21
     * @param: null
     * @return: null
     * @description: 20章优化，继承BaseProxyFactory并实现ProxyFactory#getProxy方法
     */
    public <T> T getProxy(Class<T> clazz){
        LOGGER.info("获取代理对象中，当前代理方式：jdk...");
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz}, //new Class[]{clazz}区别？
                objectProxy //20章直接取用父类BaseProxyFactory的成员变量，该变量实现了InvocationHandler
                );
    }
}
