package io.qrpc.codec;

import io.qrpc.serialization.api.Serialization;
import io.qrpc.serialization.jdk.JdkSerialization;
import io.qrpc.spi.loader.ExtensionLoader;

/**
 * @ClassName: RpcCodec
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:35
 * @Description: 编解码接口 提供序列化与反序列化的默认方法
 */

public interface RpcCodec {
    /**
     * @author: qiu
     * @date: 2024/2/28 22:09
     * @param: serializationType
     * @return: io.qrpc.serialization.api.Serialization
     * @description: 序列化类型
     * 26章SPI扩展，给定的序列化类型参数通过SPI机制加载对应的扩展类
     */
    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class,serializationType);
    }
}
