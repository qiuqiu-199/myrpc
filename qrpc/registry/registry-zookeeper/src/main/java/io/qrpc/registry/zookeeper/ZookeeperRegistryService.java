package io.qrpc.registry.zookeeper;

import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.loadBalancer.random.RandomLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @ClassName: ZookeeperRegistryService
 * @Author: qiuzhiq
 * @Date: 2024/2/22 11:16
 * @Description: 21章新增，基于Zookeeper实现注册中心并实现服务注册与发现相关的5个方法
 */

public class ZookeeperRegistryService implements RegistryService {
    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryService.class);
    //初始化CuratorFramework客户端的时候，进行连接重试的间隔时间
    public static final int BASE_SLEEP_TIME_MS = 1000;
    //初始化CuratorFramework客户端的时候，进行连接重试的最大重试次数
    public static final int MAX_RETRIES = 3;
    //服务注册到Zookeeper的根路径
    public static final String ZK_BASE_PATH = "/q_rpc";
    //服务注册与发现的ServiceDiscovery实例，用来在Zookeeper中进行服务注册和发现事宜
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    //负载均衡接口
    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;

    /**
     * @author: qiu
     * @date: 2024/2/22 11:37
     * @param: config
     * @return: void
     * @description: 21章新增，构建CuratorFramework客户端并初始化ServiceDiscovery对象 TODO 待进一步理解
     *42章，根据注册配置的负载均衡参数加载负载均衡接口的SPI实现类
     */
    @Override
    public void init(RegistryConfig config) throws Exception {
        LOGGER.info("ZookeeperRegistryService#init基于zookeeper实现的注册中心开始初始化...");
        //构建CuratorFramework客户端对象
        CuratorFramework client = CuratorFrameworkFactory.newClient(config.getRegistryAddr(), new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();

        //ServiceDiscovery对象的初始化需要、
        // 1.我们自定义好的ServiceMeta对象；
        // 2.我们创建的CuratorFramework客户端
        // 3.Json序列化器
        // 4.设置的基于Zookeeper的路径
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder
                .builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();

        //24章新增，后续SPI扩展，这里先直接new
        this.serviceLoadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class,config.getServiceLoadBalance());
    }

    /**
     * @author qiu
     * @date 2024/2/22 16:56
     * @param serviceMeta
     * @return void
     * @description 21章新增。使用ServiceDiscovery将服务元数据注册到Zookeeper里
     */
    @Override
    public void registry(ServiceMeta serviceMeta) throws Exception {
        LOGGER.info("ZookeeperRegistryService#registry注册服务中...");
        //根据ServiceMeta里的元数据创建ServiceInstance对象，然后调用ServiceDiscovery#registryService注册
        //创建ServiceInstance对象需要：
        //1.服务的key，由服务名、版本号和分组组成
        //2.服务的IP地址
        //3.服务的端口
        //4.服务元数据ServiceMeta
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();

        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * @author: qiu
     * @date: 2024/2/22 17:04
     * @param: serviceMeta
     * @return: void
     * @description: 21章新增。使用ServiceDiscovery对象移除服务注册在zookeeper中的元数据
     */
    @Override
    public void unregistry(ServiceMeta serviceMeta) throws Exception {
        LOGGER.info("ZookeeperRegistryService#unregistry注销服务中...");
        //和注册服务时一样根据ServiceMeta创建ServiceInstance对象，不过最终调用的是ServiceDiscovery的unregistryService方法
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();

        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * @author: qiu
     * @date: 2024/2/22 17:34
     * @param: serviceKey
     * @param: invokerHashcode
     * @return: io.qrpc.protocol.meta.ServiceMeta
     * @description: 21章新增。根据服务名和invokerHashcode从Zookeeper里获取服务的元数据，invokerHashcode目前没用上，预留给后续的负载均衡策略扩展。
     * 24章，使用负载均衡实现类来选择其中一个服务
     */
    @Override
    public ServiceMeta discovery(String serviceKey, int invokerHashcode,String sourceIp) throws Exception {
        LOGGER.info("ZookeeperRegistryService#discovery发现服务中...");
        //首先调用ServiceDiscovery#queryForInstance方法根据服务名获取ServiceInstance的集合，根据某种策略中集合中选取一个ServiceInstance
        //接着将ServiceInstance中的服务员数据返回
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);
        //目前使用随机策略选择ServiceInstance
        ServiceInstance<ServiceMeta> instance = this.serviceLoadBalancer.select((List<ServiceInstance<ServiceMeta>>)serviceInstances,invokerHashcode,sourceIp);
        if (instance != null) return instance.getPayload();

        return null;
    }

    /**
     * @author: qiu
     * @date: 2024/2/22 17:47
     * @param: serviceMeta
     * @return: void
     * @description: 21章新增。使用ServiceDiscovery#close方法关闭与Zookeeper的连接
     */
    @Override
    public void destory(ServiceMeta serviceMeta) throws Exception {
        LOGGER.info("ZookeeperRegistryService#destory与Zookeeper断开连接中...");
        serviceDiscovery.close();
    }
}
