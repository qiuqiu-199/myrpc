package io.qrpc.loadBalancer.helper;

import io.qrpc.protocol.meta.ServiceMeta;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @ClassName: ServiceLoadBalancerHelper
 * @Author: qiuzhiq
 * @Date: 2024/3/1 14:52
 * @Description: 6.5新增，帮助类。
 * 6.6标记已过时，方法实现太简单
 */
@Deprecated
public class ServiceLoadBalancerHelper {
    //缓存ServiceMeta
    private static volatile List<ServiceMeta> cachedServiceMetas = new CopyOnWriteArrayList<>();

    /**
     * @author: qiu
     * @date: 2024/3/1 15:02
     * @description: 6.5新增。Zookeeper相关，将ServiceInstance<ServiceMeta>列表转换为ServiceMeta列表
     */
    public static List<ServiceMeta> getServiceMetaList(List<ServiceInstance<ServiceMeta>> servers) {
        if (servers == null || servers.isEmpty()) return cachedServiceMetas;

        //先清空
        cachedServiceMetas.clear();

        //方式1：
//        servers.forEach(instance -> {
//            cachedServiceMetas.add(instance.getPayload());
//        });
//        return cachedServiceMetas;

        //方式二：使用stream
        return servers.stream().map(ServiceInstance::getPayload).collect(Collectors.toList());
    }
}
