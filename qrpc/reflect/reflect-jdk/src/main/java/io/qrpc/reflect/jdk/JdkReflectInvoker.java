package io.qrpc.reflect.jdk;

import io.qrpc.reflect.api.ReflectInvoker;
import io.qrpc.spi.annotation.SpiClass;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: JdkReflectInvoker
 * @Author: qiuzhiq
 * @Date: 2024/2/29 22:32
 * @Description:
 */
@SpiClass
public class JdkReflectInvoker implements ReflectInvoker {
    private final static Logger LOGGER = LoggerFactory.getLogger(JdkReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        LOGGER.info("调用真实方法使用的反射方式：jdk");

        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return fastMethod.invoke(serviceBean, parameters);
    }
}
