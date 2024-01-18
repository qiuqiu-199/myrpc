package io.qrpc.codec;

import io.qrpc.serialization.api.Serialization;
import io.qrpc.serialization.jdk.JdkSerialization;

/**
 * @ClassName: RpcCodec
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:35
 * @Description: 编解码接口 提供序列化与反序列化的默认方法
 */

public interface RpcCodec {
    default Serialization getJdkSerialization() {
        return new JdkSerialization();
    }
}
