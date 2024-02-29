package io.qrpc.proxy.cglib;

import io.qrpc.proxy.api.BaseProxyFactory;
import io.qrpc.proxy.api.ProxyFactory;
import io.qrpc.spi.annotation.SpiClass;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @ClassName: CglibProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/29 21:19
 * @Description: 33章新增，基于cglib实现动态代理
 */

@SpiClass
public class CglibProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final static Logger log = LoggerFactory.getLogger(CglibProxyFactory.class);

    private final Enhancer enhancer = new Enhancer();

    /**
     * @author: qiu
     * @date: 2024/2/29 21:26
     * @param: clazz
     * @return: T
     * @description: 33章新增，基于cglib实现动态代理
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("获取代理对象中，当前代理方式：cglib...");
        enhancer.setInterfaces(new Class[]{clazz});
        enhancer.setCallback(new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                return objectProxy.invoke(o, method, objects);
            }
        });
        return (T) enhancer.create();
    }
}
