package io.qrpc.sourceip.hash;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.loadBalancer.base.BaseEnhancedServiceLoadBalancer;
import io.qrpc.loadBalancer.helper.ServiceLoadBalancerHelper;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.spi.annotation.SpiClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @ClassName: SourceIpHashweightLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 16:51
 * @Description:
 */
@SpiClass
public class SourceIpHashweightLoadBalancer extends BaseEnhancedServiceLoadBalancer<ServiceMeta> {
    private final static Logger log = LoggerFactory.getLogger(SourceIpHashweightLoadBalancer.class);
    @Override
    public  ServiceMeta select(List<ServiceMeta> servers, int hashcode,String sourceIp) {
        log.info("SourceIpHashLoadBalancer#select 使用负载均衡策略：源IP哈希映射");
        if (servers == null || servers.isEmpty()) return null;

        //构建加权版的服务列表
        servers = getWeightServiceMetaList(servers);

        //如果传入ip为空，默认随机选择
        if (StringUtils.isBlank(sourceIp)) {
            Random random = new Random();
            return servers.get(random.nextInt(servers.size()));
        }
        int rescode = Math.abs(hashcode + sourceIp.hashCode());

        return servers.get(rescode % servers.size());
    }
}
