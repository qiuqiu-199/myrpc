package io.qrpc.serialization.api;

/**
 * @ClassName: Serialization
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:19
 * @Description: 序列化接口，定义序列化方法和反序列化方法
 */

public interface Serialization {
    <T> byte[] serialize(T obj);

    <T> T desrialize(byte[] data, Class<T> aClass);
}
