package io.qrpc.consumer.spring;

import io.qrpc.consumer.RpcClient;
import org.springframework.beans.factory.FactoryBean;

/**
 * @ClassName: RpcReferenceBean
 * @Author: qiuzhiq
 * @Date: 2024/3/6 13:08
 * @Description: 服务消费者bean，用于注册RpcClient组件，通过RpcClient组件创建代理对象，再通过FactoryBean接口获取.
 * 总结最大作用就是创建服务代理对象注册到容器中。
 * 实现FactoryBean接口也是一种懒加载，需要获取对象的时候才会真正调用getObject方法去创建对象。
 * 所以FactoryBean接口就是用来创建一些比较复杂的bean，比如我们要创建的代理对象就要先创建RpcClient，再通过其create方法来创建。
 */

public class RpcReferenceBean implements FactoryBean {
    //要调用的远程接口的信息：接口类、服务版本、服务分组
    private Class<?> interfaceClass;
    private String version;
    private String group;
    //注册中心相关：注册中心类型、注册中心地址、注册中心使用的负载均衡策略
    private String registryType;
    private String registryAddr;
    private String registryLoadbalanceType;
    //序列化类型
    private String serializationType;
    //代理类型与代理对象
    private String proxyType;
    private Object proxyObject;
    //超时时间
    private long timeout;
    //是否同步调用、是否异步调用
    private boolean async;
    private boolean oneway;
    //心跳机制相关：心跳间隔时间、扫描非活跃连接间隔时间
    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;
    //重试机制相关，重试次数、重试间隔时间
    private int maxRetryTimes = 3;
    private int retryInterval = 3000;

    //重写BeanFactory的两个方法
    @Override
    public Object getObject() throws Exception {
        return proxyObject;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    //用来生成当前类的BeanDefinition和bean
    public void init() {
        RpcClient rpcClient = new RpcClient(version, group, registryType, registryAddr, registryLoadbalanceType, serializationType, proxyType, async, oneway, timeout, heartbeatInterval, scanNotActiveChannelInterval, maxRetryTimes, retryInterval);
        this.proxyObject = rpcClient.create(interfaceClass);
    }

    //除了proxyObject外的15个属性的set方法
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public void setRegistryLoadbalanceType(String registryLoadbalanceType) {
        this.registryLoadbalanceType = registryLoadbalanceType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
