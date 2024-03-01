package io.qrpc.consistentHash;

import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.spi.annotation.SpiClass;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName: ZkConsistentHashLoadBalancer
 * @Author: qiuzhiq
 * @Date: 2024/3/1 10:32
 * @Description: 6.4新增，扩展一致性哈希实现的负载均衡实现。
 */
@SpiClass
public class ZkConsistentHashLoadBalancer implements ServiceLoadBalancer<ServiceInstance<ServiceMeta>> {
    private final static Logger log = LoggerFactory.getLogger(ZkConsistentHashLoadBalancer.class);

    private final int VIRTUAL_NODE_NUM = 10;
    private final String VIRTUAL_NODE_SPLIT = "#";

    @Override
    public ServiceInstance<ServiceMeta> select(List<ServiceInstance<ServiceMeta>> servers, int hashcode, String sourceIp) {
        log.info("ZkConsistentHashLoadBalancer#select 使用负载均衡策略：一致性哈希");
        //先构造哈希环
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = makeConsistentHashRing(servers);
        //根据hashCode选择哈希环的对应服务节点
        return allocate(ring, hashcode);
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 10:49
     * @param: ring
     * @param: hashcode
     * @return: org.apache.curator.x.discovery.ServiceInstance<io.qrpc.protocol.meta.ServiceMeta>
     * @description: 6.4新增。基于哈希环选择节点
     */
    private ServiceInstance<ServiceMeta> allocate(TreeMap<Integer, ServiceInstance<ServiceMeta>> ring, int hashcode) {
        //ceilingEntry：找到第一个大于等于给定值的节点
        Map.Entry<Integer, ServiceInstance<ServiceMeta>> entry = ring.ceilingEntry(hashcode);
        //如果没有节点说明没有对应的服务
        if (entry == null) {
            entry = ring.firstEntry();
            throw new RuntimeException("not discover useful service, you need registry service in registry center first !");
        }
        return entry.getValue();
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 10:53
     * @param: servers
     * @return: java.util.TreeMap<java.lang.Integer, org.apache.curator.x.discovery.ServiceInstance < io.qrpc.protocol.meta.ServiceMeta>>
     * @description: 6.4新增，根据服务列表构建哈希环。
     */
    private TreeMap<Integer, ServiceInstance<ServiceMeta>> makeConsistentHashRing(List<ServiceInstance<ServiceMeta>> servers) {
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = new TreeMap<>();
        for (ServiceInstance<ServiceMeta> instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                ServiceMeta payload = instance.getPayload();
                //键由服务地址、服务端口、分隔符#、虚拟节点序号组成
                int key = String.join(":",
                        payload.getServiceAddr(),
                        String.valueOf(payload.getServicePort()),
                        VIRTUAL_NODE_SPLIT,
                        String.valueOf(i)
                ).hashCode();

                //值为服务点，实际上虚拟节点对应的都是服务，与之前的对一致性哈希的印象不太一样，后面看能不能改
                ring.put(key, instance);
            }
        }
        return ring;
    }
}
