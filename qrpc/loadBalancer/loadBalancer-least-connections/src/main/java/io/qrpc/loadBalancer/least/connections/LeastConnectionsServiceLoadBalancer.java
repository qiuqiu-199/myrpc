package io.qrpc.loadBalancer.least.connections;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.loadBalancer.context.ConnectionsContext;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: LeastConnectionsServiceLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 19:59
 * @Description: 6.5节新增，实现最少连接数的负载策略。
 */
@SpiClass
public class LeastConnectionsServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {
    private final static Logger log = LoggerFactory.getLogger(LeastConnectionsServiceLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashcode, String sourceIp) {
        log.info("LeastConnectionsServiceLoadBalancer#select 使用负载均衡策略：最少连接数");

        if (servers == null || servers.isEmpty()) return null;

        //先视图获取没有连接的服务
        ServiceMeta serviceMeta = this.getNullServiceMeta(servers);
        //如果没有这样的服务，就选择连接数最少的服务
        if (serviceMeta == null) {
            serviceMeta = this.getServiceMeta(servers);
        }
        return serviceMeta;
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 20:18
     * @param: servers
     * @return: io.qrpc.protocol.meta.ServiceMeta
     * @description: 6.5节新增，获取无连接的服务。
     */
    private ServiceMeta getNullServiceMeta(List<ServiceMeta> servers) {
        for (int i = 0; i < servers.size(); i++) {
            ServiceMeta serviceMeta = servers.get(i);
            if (ConnectionsContext.getConnection(serviceMeta) == null) {
                return serviceMeta;
            }
        }
        return null;
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 20:17
     * @param: servers
     * @return: io.qrpc.protocol.meta.ServiceMeta
     * @description: 6.5新增，获取连接数最少的服务
     */
    private ServiceMeta getServiceMeta(List<ServiceMeta> servers) {
        ServiceMeta meta = servers.get(0);
        Integer minCount = ConnectionsContext.getConnection(meta);

        for (int i = 1; i < servers.size(); i++) {
            ServiceMeta serviceMeta = servers.get(i);
            Integer curCount = ConnectionsContext.getConnection(serviceMeta);
            if (curCount < minCount) {
                meta = serviceMeta;
                minCount = curCount;
            }
        }
        return meta;
    }
}
