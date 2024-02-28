package io.qrpc.spi.factory;

import io.qrpc.spi.annotation.SPI;
import io.qrpc.spi.annotation.SpiClass;
import io.qrpc.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * @ClassName: SpiExtensionFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/27 14:22
 * @Description: 25章新增，实现ExtensionFactory接口，作用暂不清楚。
 */
@SpiClass
public class SpiExtensionFactory implements ExtensionFactory {
    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
        return Optional.ofNullable(clazz)//创建一个Optional对象，对象的值的类型为Class，对象的值可以为null
                .filter(Class::isInterface)//过滤出clazz为接口的Class对象
                .filter(cls -> cls.isAnnotationPresent(SPI.class))//过滤出标注了SPI注解的接口的Class对象
                //此时的Class对象是SPI接口的Class对象，调用ExtensionLoader::getExtensionLoader获取接口对应的扩展类加载器ExtensionLoader
                .map(ExtensionLoader::getExtensionLoader)
                //此时Optional里是接口的扩展类加载器，调用方法获取默认的扩展类对象
                .map(ExtensionLoader::getDefaultSpiClassInstance)
                //如果前一步没有加载出扩展类对象，那么返回null
                .orElse(null);
    }
}
