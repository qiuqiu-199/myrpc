package io.qrpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qrpc.common.exception.SerializerException;
import io.qrpc.serialization.api.Serialization;
import io.qrpc.spi.annotation.SpiClass;
import org.apache.commons.lang3.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @ClassName: JsonSerialization
 * @Author: qiuzhiq
 * @Date: 2024/2/29 10:40
 * @Description: 27章新增，基于Jackson依赖实现JSON序列化方式
 */
@SpiClass
public class JsonSerialization implements Serialization {
    private final static Logger log = LoggerFactory.getLogger(JsonSerialization.class);

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        //对对象的时间字段，Jackson的默认时间格式是yyyy-MM-ddTHH:mm:ss.SSSZ，如果和要序列化的对象的时间字段的格式不一样会产生序列化失败的问题
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        //字符串带上缩进，给人看
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //序列化时忽略值为null的字段
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        //配置 TODO 待进一步理解
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);


        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        //序列化时，如果对象为null，不抛异常，也就是忽略
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //反序列化时，忽略JSON字符串中存在但是java对象中不存在的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        log.info("execute json serialize...");
        if (obj == null) throw new SerializerException("serialize obj is null!");

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }

        return bytes;
    }

    @Override
    public <T> T desrialize(byte[] data, Class<T> aClass) {
        log.info("execute json deSerialize...");
        if (data == null) throw new SerializationException("desrialize data is null !");

        T obj;
        try {
            obj = mapper.readValue(data, aClass);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage(), e);
        }
        return obj;
    }
}
