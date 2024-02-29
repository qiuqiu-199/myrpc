package io.qrpc.serialization.api;

import io.qrpc.constants.RpcConstants;
import io.qrpc.spi.annotation.SPI;

/**
 * @ClassName: Serialization
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:19
 * @Description: 序列化接口，定义序列化方法和反序列化方法
 * 26章修改，标注SPI注解，并且值设置为jdk，如果没有指定序列化类型，则默认序列化类型为jdk序列化
 */
@SPI(RpcConstants.SERIALIZATION_JDK)
public interface Serialization {
    <T> byte[] serialize(T obj);

    <T> T desrialize(byte[] data, Class<T> aClass);
}
