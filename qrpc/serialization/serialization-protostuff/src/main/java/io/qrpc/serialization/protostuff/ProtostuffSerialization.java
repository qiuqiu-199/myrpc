package io.qrpc.serialization.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.qrpc.serialization.api.Serialization;
import io.qrpc.spi.annotation.SpiClass;
import org.apache.commons.lang3.SerializationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: ProtostuffSerialization
 * @Author: qiuzhiq
 * @Date: 2024/2/29 14:24
 * @Description: 31章新增，实现基于Protostuff的序列化方式
 */

@SpiClass
public class ProtostuffSerialization implements Serialization {
    private final static Logger log = LoggerFactory.getLogger(ProtostuffSerialization.class);

    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    private Objenesis objenesis = new ObjenesisStd();


    /**
     * @author: qiu
     * @date: 2024/2/29 14:42
     * @param: cls
     * @return: com.dyuproject.protostuff.Schema<T>
     * @description: 31章新增，根据Class对象获取对应的约束，约束用于生成Proto Buffer格式的数据
     */
    @SuppressWarnings("unchecked") //忽略警告
    private <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.putIfAbsent(cls, schema);
        }
        return schema;
    }

    @SuppressWarnings("unchecked") //忽略警告
    @Override
    public <T> byte[] serialize(T obj) {
        log.info("execute protostuff deSerialize...");
        if (obj == null) throw new SerializationException("serialize objetc is null !");

        Class<T> aClass = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(aClass);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T desrialize(byte[] data, Class<T> aClass) {
        log.info("execute protostuff serialize...");
        if (data == null) throw new SerializationException("deSeriaze data is null !");

        try {
            T message = objenesis.newInstance(aClass);
            Schema<T> schema = getSchema(aClass);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
