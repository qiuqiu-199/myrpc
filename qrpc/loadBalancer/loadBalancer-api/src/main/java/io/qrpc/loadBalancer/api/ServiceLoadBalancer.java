package io.qrpc.loadBalancer.api;

import io.qrpc.constants.RpcConstants;
import io.qrpc.spi.annotation.SPI;

import java.util.List;

/**
 * @InterfaceName: ServiceLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/2/26 16:25
 * @Description: 24章新增，负载均衡通用接口
 */
@SPI(RpcConstants.SERVICE_LOAD_BALANCER_RANDOM)
public interface ServiceLoadBalancer<T> {
    /**
     * @author: qiu
     * @date: 2024/2/26 17:05
     * @param: servers 可用服务集合
     * @param: hashcode 哈希值
     * @return: T
     * @description: 24章新增，负载均衡方法
     */
    T select(List<T> servers, int hashcode,String sourceIp);
}
