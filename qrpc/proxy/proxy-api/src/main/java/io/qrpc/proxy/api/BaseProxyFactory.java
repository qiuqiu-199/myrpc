package io.qrpc.proxy.api;

import io.qrpc.proxy.api.config.ProxyConfig;
import io.qrpc.proxy.api.object.ObjectProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: BaseProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/21 22:53
 * @Description: 20章新增，代理工厂接口的基类，是一个抽象类
 */

public abstract class BaseProxyFactory<T> implements ProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProxyFactory.class);
    protected ObjectProxy<T> objectProxy;

    /**
     * @author: qiu
     * @date: 2024/2/21 22:55
     * @param: config
     * @return: void
     * @description: 20章，抽象基类实现init方法，创建代理对象
     */
    @Override
    public <T> void init(ProxyConfig<T> config) {
        LOGGER.info("BaseProxyFactory#init 代理工厂初始化中，创建ObjectProxy...");
        this.objectProxy = new ObjectProxy(
                config.getClazz(),
                config.getServiceVersion(),
                config.getServiceGroup(),
                config.getSerializationType(),
                config.getTimeout(),
                config.getConsumer(),
                config.isAsync(),
                config.isOneway(),
                config.getRegistryService()
                );
    }
}
