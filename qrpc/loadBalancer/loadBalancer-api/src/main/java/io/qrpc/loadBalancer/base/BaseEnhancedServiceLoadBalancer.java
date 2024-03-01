package io.qrpc.loadBalancer.base;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @ClassName: BaseEnhancedServiceLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 14:35
 * @Description: 6.5节新增，抽象类，提供BaseEnhancedServiceLoadBalancer方法返回一个根据权重重新生成的服务列表
 */

public abstract class BaseEnhancedServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    /**
     * @author: qiu
     * @date: 2024/3/1 14:41
     * @param: serviceMetas
     * @return: java.util.List<io.qrpc.protocol.meta.ServiceMeta>
     * @description: 6.5节新增，核心逻辑： 根据权重重新生成服务元数据列表，权重越高的元数据，会在最终的列表中出现的次数越多。例如，权重为1，最终出现1次，权重为2，最终出现2次，权重为3，最终出现3次，依此类推.
     */
    protected List<ServiceMeta> getWeightServiceMetaList(List<ServiceMeta> serviceMetas) {
        if (serviceMetas == null || serviceMetas.isEmpty()) return new ArrayList<>();

        List<ServiceMeta> list = new ArrayList<>();

        serviceMetas.forEach(meta -> {
            IntStream.range(0, meta.getWeight()).forEach(i -> {
                list.add(meta);
            });
        });

        return list;
    }
}
