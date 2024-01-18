package io.qrpc.serialization.jdk;

import io.qrpc.common.exception.SerializerException;
import io.qrpc.serialization.api.Serialization;

import java.io.*;

/**
 * @ClassName: JdkSerialization
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:22
 * @Description: 基于jdk的序列化
 */

public class JdkSerialization implements Serialization {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) throw new SerializerException("serialize obj is null!");

        //序列化输出
        try {
            ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            ObjectOutputStream objout = new ObjectOutputStream(byteout);

            objout.writeObject(obj);

            return byteout.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T desrialize(byte[] data, Class<T> aClass) {
        if (data == null) throw new SerializerException("deserialize data is null");

        //反序列化
        try {
            ByteArrayInputStream bytein = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(bytein);

            return (T) in.readObject();

        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
