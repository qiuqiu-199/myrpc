package io.qrpc.reflect.cglib;

import io.qrpc.reflect.api.ReflectInvoker;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @ClassName: CglibReflectInvoker
 * @Author: qiuzhiq
 * @Date: 2024/2/29 22:34
 * @Description:
 */
@SpiClass
public class CglibReflectInvoker implements ReflectInvoker {
    private final static Logger LOGGER = LoggerFactory.getLogger(CglibReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        LOGGER.info("服务提供者调用真实方法使用的反射方式：cglib");
        Method method = serviceClass.getMethod(methodName, parameterTypes);

        method.setAccessible(true);

        return method.invoke(serviceBean, parameters);
    }
}
