package io.qrpc.registry.api;

import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.registry.api.config.RegistryConfig;

/**
 * @InterfaceName: RegistryService
 * @Author: qiuzhiq
 * @Date: 2024/2/22 11:01
 * @Description: 21章，注册中心顶层接口，定义了5个服务注册与发现相关方法，基于zookeeper或者其他的注册中心需要实现这些方法
 */

public interface RegistryService {
    /**
     * @author: qiu
     * @date: 2024/2/22 11:08
     * @param: serviceMeta
     * @return: void
     * @description: 服务注册
     */
    void registry(ServiceMeta serviceMeta) throws Exception;
    /**
     * @author: qiu
     * @date: 2024/2/22 11:10
     * @param: serviceMeta
     * @return: void
     * @description: 服务注销
     */
    void unregistry(ServiceMeta serviceMeta) throws  Exception;
    /**
     * @author: qiu
     * @date: 2024/2/22 11:10
     * @param: serviceName
     * @param: invokerHashcode
     * @return: io.qrpc.protocol.meta.ServiceMeta
     * @description: 服务发现
     */
    ServiceMeta discovery(String serviceKey, int invokerHashcode,String sourceIp) throws Exception;
    /**
     * @author: qiu
     * @date: 2024/2/22 11:11
     * @param: serviceMeta
     * @return: void
     * @description: 注册中心关闭
     */
    void destory(ServiceMeta serviceMeta) throws Exception;
    /**
     * @author: qiu
     * @date: 2024/2/22 11:11
     * @param: config
     * @return: void
     * @description: 注册中心初始化，由子类实现，创建对应子类对象时需要调用本方法进行初始化
     */
    default void init(RegistryConfig config) throws Exception{}
}
