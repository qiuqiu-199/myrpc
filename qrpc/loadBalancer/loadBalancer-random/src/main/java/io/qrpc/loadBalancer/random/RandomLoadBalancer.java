package io.qrpc.loadBalancer.random;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @ClassName: RandomLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/2/26 16:27
 * @Description: 24章新增，基于随机算法的负载均衡策略
 */
@SpiClass
public class RandomLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomLoadBalancer.class);
    /**
     * @author: qiu
     * @date: 2024/2/26 16:37
     * @param: servers
     * @param: hashcode
     * @return: T
     * @description: 基于随机算法选择一个提供者实例
     */
    @Override
    public T select(List<T> servers, int hashcode,String sourceIp) {
        LOGGER.info("RandomLoadBalancer#select 使用负载均衡策略--随机...");

        if (servers == null || servers.isEmpty()) return null;

        Random random = new Random();
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
