package io.qrpc.sourceip.hash;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.spi.annotation.SpiClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: SourceIpHashLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 9:26
 * @Description: 6.4节新增，实现基于源IP哈希映射的负载均衡策略
 */
@SpiClass
public class SourceIpHashLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final static Logger log = LoggerFactory.getLogger(SourceIpHashLoadBalancer.class);
    @Override
    public  T select(List<T> servers, int hashcode,String sourceIp) {
        log.info("SourceIpHashLoadBalancer#select 使用负载均衡策略：源IP哈希映射");
        if (servers == null || servers.isEmpty()) return null;

        //如果传入ip为空，默认第一个服务
        if (StringUtils.isBlank(sourceIp)) return servers.get(0);
        int rescode = Math.abs(hashcode + sourceIp.hashCode());
        return servers.get(rescode % servers.size());
    }
}
