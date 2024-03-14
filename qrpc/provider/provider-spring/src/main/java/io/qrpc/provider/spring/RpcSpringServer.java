package io.qrpc.provider.spring;

import io.qrpc.annotation.RpcService;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.provider.common.server.base.BaseServer;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @ClassName: RpcSpringServer
 * @Author: qiuzhiq
 * @Date: 2024/3/6 9:57
 * @Description: 基于spring实现RPC服务端，实现ApplicationContextAware接口以注册服务，实现InitializingBean接口以启动netty服务端。
 */

public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {
    private final static Logger log = LoggerFactory.getLogger(RpcSpringServer.class);

    /**
     * @author: qiu
     * @date: 2024/2/24 16:59
     * @description: 22章修改，构造方法根据传入的注册中心地址和注册中心类型引入注册中心
     * 42章，构造方法增加负载均衡参数
     */
    public RpcSpringServer(String serverAddr,
                           String registryType,
                           String registryAddr,
                           String registryLoadBalanceType,
                           String reflectType,
                           int heartbeatInterval,
                           int scanNotActiveChannelInterval,
                           boolean enableCacheResult,
                           int cacheResultExpire,
                           int maxConnectionCount,
                           String disuseStrategyType
    ) {
        super(
                serverAddr,
                registryType,
                registryAddr,
                registryLoadBalanceType,
                reflectType,
                heartbeatInterval,
                scanNotActiveChannelInterval,
                enableCacheResult,
                cacheResultExpire,
                maxConnectionCount,
                disuseStrategyType
        );
    }

    /**
     * @author: qiu
     * @date: 2024/3/6 10:31
     * @description: 9章新增，实现ApplicationContextAware接口。spring扫描生成实例后，获取@RpcService注解，创建服务元数据并注册到注册中心。
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        //获取标注了RpcService注解的类的实例
        //注意：@RpcService注解携带了@Component注解，所以标注@RpcService注解的类也会注册为组件
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            //将服务注册到注册中心
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService annotation = serviceBean.getClass().getAnnotation(RpcService.class);

                //缓存服务及其对应的实例
                handlerMap.put(RpcServiceHelper.buildServiceKey(this.getServiceName(annotation), annotation.version(), annotation.group()), serviceBean);

                //获取权重，偕同其他注解信息一起创建服务元数据
                int weight = annotation.weight();
                if (weight < RpcConstants.SERVICE_WEIGHT_MIN) weight = RpcConstants.SERVICE_WEIGHT_MIN;
                if (weight > RpcConstants.SERVICE_WEIGHT_MAX) weight = RpcConstants.SERVICE_WEIGHT_MAX;
                ServiceMeta serviceMeta = new ServiceMeta(this.getServiceName(annotation), annotation.version(), annotation.group(), host, port, weight);

                //注册服务
                try {
                    registryService.registry(serviceMeta);
                } catch (Exception e) {
                    log.error("@rpcService注解扫描出错：", e);
                }
            }
        }
    }

    /**
     * @author: qiu
     * @date: 2024/2/24 11:39
     * @description: 根据注解RpcService获取服务名，优先使用interfaceClass.getName()，如果没有就使用interfaceClassName
     * 方法来源RpcServiceScanner类
     */
    private String getServiceName(RpcService rpcService) {
        Class<?> clazz = rpcService.interfaceClass();
        if (clazz == void.class) return rpcService.interfaceClassName();
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty())
            serviceName = rpcService.interfaceClassName();
        return serviceName;
    }

    /**
     * @author: qiu
     * @date: 2024/3/6 10:33
     * @description: 设置完了后，启动netty服务端
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        startNettyServer();
    }
}
