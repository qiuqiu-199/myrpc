package io.qrpc.reflect.api;

import io.qrpc.spi.annotation.SPI;

/**
 * @InterfaceName: ReflectInvoker
 * @Author: qiuzhiq
 * @Date: 2024/2/29 22:19
 * @Description: 37章新增，SPI接口，扩展反射方式
 */
@SPI
public interface ReflectInvoker {
    /**
     * @author: qiu
     * @date: 2024/2/29 22:22
     * @param: serviceBean 方法所在对象的实例
     * @param: serviceClass 方法所在对象的实例的Class对象
     * @param: methodName 要调用的方法名
     * @param: parameterTypes 方法参数数组
     * @param: parameters 方法参数数组
     * @return: java.lang.Object 方法执行结果
     * @description:
     */
    Object invokeMethod(
            Object serviceBean,
            Class<?> serviceClass,
            String methodName,
            Class<?>[] parameterTypes,
            Object[] parameters
            ) throws Throwable;
}
