package io.qrpc.loadBalancer.robin;

import io.qrpc.loadBalancer.base.BaseEnhancedServiceLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: RobinWeightLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 16:36
 * @Description:
 */
@SpiClass
public class RobinWeightLoadBalancer extends BaseEnhancedServiceLoadBalancer<ServiceMeta> {
    private final static Logger log = LoggerFactory.getLogger(RobinWeightLoadBalancer.class);
    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashcode, String sourceIp) {
        log.info("RobinWeightLoadBalancer#select 使用负载均衡策略--加权轮询...");
        if (servers == null || servers.isEmpty()) return null;

        //构建加权版的服务列表
        servers = getWeightServiceMetaList(servers);

        int index = atomicInteger.incrementAndGet();
        if (index >= Integer.MAX_VALUE - 10000) atomicInteger.set(0);
        int count = servers.size();

        return servers.get(index % count);
    }
}
