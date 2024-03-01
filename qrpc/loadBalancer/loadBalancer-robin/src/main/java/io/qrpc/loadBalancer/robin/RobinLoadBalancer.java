package io.qrpc.loadBalancer.robin;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: RobinLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 8:57
 * @Description: 44章，基于轮询实现的负载均衡接口的SPI实现类
 */
@SpiClass
public class RobinLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final static Logger log = LoggerFactory.getLogger(RobinLoadBalancer.class);

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public T select(List<T> servers, int hashcode,String sourceIp) {
        log.info("RobinLoadBalancer#select 使用负载均衡策略--轮询...");
        if (servers == null || servers.isEmpty()) return null;

        int index = atomicInteger.incrementAndGet();
        if (index >= Integer.MAX_VALUE -10000) atomicInteger.set(0);
        int count = servers.size();
        return servers.get(index % count);
    }
}
