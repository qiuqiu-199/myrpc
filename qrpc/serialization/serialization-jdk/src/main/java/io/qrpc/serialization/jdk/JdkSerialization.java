package io.qrpc.serialization.jdk;

import io.qrpc.common.exception.SerializerException;
import io.qrpc.serialization.api.Serialization;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @ClassName: JdkSerialization
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:22
 * @Description: 基于jdk的序列化
 * 26章，标注SpiClass注解，表示这是一个SPI实现类
 */
@SpiClass
public class JdkSerialization implements Serialization {
    private final static Logger log = LoggerFactory.getLogger(JdkSerialization.class);
    @Override
    public <T> byte[] serialize(T obj) {
        log.error("execute jdk serialize...");

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
        log.error("execute jdk deSerialize...");
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
