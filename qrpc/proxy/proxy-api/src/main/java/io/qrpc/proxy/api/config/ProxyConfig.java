package io.qrpc.proxy.api.config;

import io.qrpc.proxy.api.consumer.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: ProxyConfig
 * @Author: qiuzhiq
 * @Date: 2024/2/21 22:46
 * @Description: 20章新增，用于配置动态代理
 */
@Data
@AllArgsConstructor
public class ProxyConfig<T> implements Serializable {
    private static final long serialVersionUID = 2447008678604797411L;
    private Class<T> clazz;
    private String serviceVersion;
    private String serviceGroup;
    private String serializationType;
    private long timeout;
    private boolean async;
    private boolean oneway;
    private Consumer consumer;
}
