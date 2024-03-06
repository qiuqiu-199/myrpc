package io.qrpc.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.constant.Constants;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.loadBalancer.api.ServiceLoadBalancer;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.spi.annotation.SpiClass;
import io.qrpc.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @ClassName: NacosRegistryService
 * @Author: qiuzhiq
 * @Date: 2024/3/2 10:05
 * @Description: 6.6节新增，基于Nacos实现的注册中心
 */
@SpiClass
public class NacosRegistryService implements RegistryService {
    private static final Logger logger = LoggerFactory.getLogger(NacosRegistryService.class);

    //连接nacos的重试次数和重试间隔时间
    private final static int MAX_RETRIES = 3;
    private final static int RETRY_INTERVAL_TIME = 1000;

    private NamingService namingService;

    //负载均衡接口
    private ServiceLoadBalancer<ServiceMeta> serviceLoadBalancer;
    @Override
    public void registry(ServiceMeta serviceMeta) throws Exception {
        logger.info("NacosRegistryService#registry 注册服务中...");

        //serviceMeta转换为Instance后进行注册
        Instance instance = wrapServiceMeta2Instance(serviceMeta);

        namingService.registerInstance(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(),serviceMeta.getServiceVersion(),serviceMeta.getServiceGroup()),instance);
    }

    @Override
    public void unregistry(ServiceMeta serviceMeta) throws Exception {
        logger.info("NacosRegistryService#unregistry 注销服务中...");

        Instance instance = wrapServiceMeta2Instance(serviceMeta);

        namingService.deregisterInstance(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(),serviceMeta.getServiceVersion(),serviceMeta.getServiceGroup()),instance);
    }

    @Override
    public ServiceMeta discovery(String serviceKey, int invokerHashcode, String sourceIp) throws Exception {
        logger.info("NacosRegistryService#discovery 发现服务中...");

        List<Instance> instances = namingService.getAllInstances(serviceKey);
        if (instances == null || instances.isEmpty()) return null;

        //这段stream用法的意思是，对instances列表的每个instance调用wrapInstance2ServiceMeta方法将instance转换为serviceMeta后形成新的列表
        List<ServiceMeta> servers = instances.stream().map(this::wrapInstance2ServiceMeta).collect(Collectors.toList());

        return serviceLoadBalancer.select(servers,invokerHashcode,sourceIp);
    }

    @Override
    public void destory(ServiceMeta serviceMeta) throws Exception {
        logger.info("NacosRegistryService#destory 与nacos断开连接中...");
        namingService.shutDown();
    }

    @Override
    public void init(RegistryConfig config) throws Exception {
        logger.info("NacosRegistryService#init 基于nacos实现的注册中心开始初始化...");
        //连接nacos客户端
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR,config.getRegistryAddr());
        //奇怪的点：加了下面这行的话，在控制台就看不到服务列表了
//        properties.put(PropertyKeyConst.NAMESPACE,"q_rpc");
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                namingService = NacosFactory.createNamingService(properties);
                //如果namingService的状态为up并且测试没有问题说明连接成功
                if (Constants.HealthCheck.UP.equals(namingService.getServerStatus()) &&
                    testNamingService(namingService)){
                    break;
                }else {
                    logger.warn("第"+i+"次连接nacos服务端失败，重新连接中...");
                }
                //连接失败后关闭namingService，并休眠一秒再重连
                namingService.shutDown();
                namingService = null;
                Thread.sleep(RETRY_INTERVAL_TIME);
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }
        if (namingService == null){
            throw new IllegalStateException("创建nacos客户端失败！namingService = null !");
        }

        //SPI加载负载均衡扩展类
        this.serviceLoadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class,config.getServiceLoadBalance());
    }

    //ServiceMeta转为Instance
    private Instance wrapServiceMeta2Instance(ServiceMeta serviceMeta){
        Instance instance = new Instance();
        //nacos的instance只有直接设置服务名、服务IP、服务端口和服务权重这4种属性的方法
        //如果要另外设置服务版本和服务分组需要使用instance自带的map来存放
        instance.setServiceName(serviceMeta.getServiceName());
        instance.setIp(serviceMeta.getServiceAddr());
        instance.setPort(serviceMeta.getServicePort());
        instance.setWeight(serviceMeta.getWeight());

        HashMap<String, String> extraMeta = new HashMap<>();
        extraMeta.put("version",serviceMeta.getServiceVersion());
        extraMeta.put("group",serviceMeta.getServiceGroup());
        instance.setMetadata(extraMeta);

        return instance;
    }
    //Instance转为ServiceMeta
    private ServiceMeta wrapInstance2ServiceMeta(Instance instance) {
        return new ServiceMeta(
                instance.getServiceName(),
                instance.getMetadata().get("version"),
                instance.getMetadata().get("group"),
                instance.getIp(),
                instance.getPort(),
                (int)instance.getWeight()
        );
    }

    //简单测试nacos连接，如果正常就返回true
    private boolean testNamingService(NamingService namingService) {
        try {
            namingService.getAllInstances("Dubbo-Nacos-Test",false);
            return true;
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return false;
    }
}
