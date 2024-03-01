package io.qrpc.loadBalancer.random;

import io.qrpc.loadBalancer.base.BaseEnhancedServiceLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @ClassName: RandomWeightLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 15:09
 * @Description:
 */
@SpiClass
public class RandomWeightLoadBalancer extends BaseEnhancedServiceLoadBalancer<ServiceMeta> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomWeightLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashcode, String sourceIp) {
        LOGGER.info("RandomWeightLoadBalancer#select 使用负载均衡策略--加权随机...");
        if (servers == null || servers.isEmpty()) return null;

        //构建加权版的服务列表
        servers = getWeightServiceMetaList(servers);

        Random random = new Random();
        int index = random.nextInt(servers.size());

        return servers.get(index);
    }
}
