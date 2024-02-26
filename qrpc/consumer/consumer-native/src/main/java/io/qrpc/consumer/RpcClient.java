package io.qrpc.consumer;

import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import io.qrpc.common.exception.RegistryException;
import io.qrpc.consumer.common.RpcConsumer;
import io.qrpc.proxy.api.ProxyFactory;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.config.ProxyConfig;
import io.qrpc.proxy.api.object.ObjectProxy;
import io.qrpc.proxy.jdk.JdkProxyFactory;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.registry.zookeeper.ZookeeperRegistryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RpcClient
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:05
 * @Description:
 */

public class RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String serviceVersion;
    private String serviceGroup;
    private String seriliazationType;
    private long timeout;
    private boolean async;
    private boolean oneway;
    private RegistryService registryService;

    public RpcClient(
            String registryAddress,
            String registryType,
            String serviceVersion,
            String serviceGroup,
            String seriliazationType,
            long timeout,
            boolean async,
            boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.seriliazationType = seriliazationType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress,registryType);
    }

    /**
     * @author: qiu
     * @date: 2024/2/25 14:28
     * @param: registryAddress
     * @param: registryType
     * @return: io.qrpc.registry.api.RegistryService
     * @description:
     */
    private RegistryService getRegistryService(String registryAddress, String registryType) {
        if (StringUtils.isEmpty(registryType)) throw new IllegalArgumentException("未指定registryType！！");
        //23章，后续使用SPI扩展，目前先使用zookeeper
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress,registryType));
        } catch (Exception e) {
            LOGGER.error("RpcClient初始化registryService失败：{}",e);
            throw new RegistryException(e.getMessage(),e);
        }
        return registryService;
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 23:26
     * @param: null
     * @return: null
     * @description: 20章优化，通过ProxyFactory接收基于jdk实现的动态代理工厂对象，并初始化代理工厂对象，然后返回代理工厂对象创建的代理对象
     */
    public <T> T create(Class<T> interfaceClass){
        LOGGER.info("RpcClient#create RPC客户端创建代理工厂并创建同步代理对象...");
        //多传入一个Consumer对象
        ProxyFactory proxyFactory = new JdkProxyFactory<T>();//使用ProxyFactory接口接收代理工厂对象在一定程度上具备了扩展性，为后续SPI技术打下基础
        proxyFactory.init(  //初始化生成ObjectProxy对象
                new ProxyConfig<>(
                        interfaceClass,
                        serviceVersion,
                        serviceGroup,
                        seriliazationType,
                        timeout,
                        async,
                        oneway,
                        RpcConsumer.getInstance(),
                        registryService)
        );
        return proxyFactory.getProxy(interfaceClass);
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 16:42
     * @param: interfaceClass
     * @return: io.qrpc.proxy.api.async.IAsyncObjectProxy
     * @description: 19章，构建异步化调用对象
     */
    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass){
        LOGGER.info("RpcClient#createAsync RPC客户端创建异步化代理对象...");
        return new ObjectProxy<T>(
                interfaceClass,
                serviceVersion,
                serviceGroup,
                seriliazationType,
                timeout,
                RpcConsumer.getInstance(),
                async,
                oneway,
                registryService
        );
    }

    public void shutdown(){
        LOGGER.info("RpcClient#shutdown...");
        RpcConsumer.getInstance().close();
    }
}