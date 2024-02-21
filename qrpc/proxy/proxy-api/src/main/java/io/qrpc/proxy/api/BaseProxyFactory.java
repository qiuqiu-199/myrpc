package io.qrpc.proxy.api;

import io.qrpc.proxy.api.config.ProxyConfig;
import io.qrpc.proxy.api.object.ObjectProxy;

/**
 * @ClassName: BaseProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/21 22:53
 * @Description: 20章新增，代理工厂接口的基类，是一个抽象类
 */

public abstract class BaseProxyFactory<T> implements ProxyFactory {
    protected ObjectProxy<T> proxy;

    /**
     * @author: qiu
     * @date: 2024/2/21 22:55
     * @param: config
     * @return: void
     * @description: 20章，抽象基类实现init方法，创建代理对象
     */
    @Override
    public <T> void init(ProxyConfig<T> config) {
        this.proxy = new ObjectProxy(
                config.getClazz(),
                config.getServiceVersion(),
                config.getServiceGroup(),
                config.getTimeout(),
                config.getConsumer(),
                config.getSerializationType(),
                config.isAsync(),
                config.isOneway()
                );
    }
}
