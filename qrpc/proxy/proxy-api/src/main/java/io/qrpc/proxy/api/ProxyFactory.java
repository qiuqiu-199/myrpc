package io.qrpc.proxy.api;

import io.qrpc.proxy.api.config.ProxyConfig;
import io.qrpc.spi.annotation.SPI;

/**
 * @ClassName: ProxyFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/21 22:50
 * @Description: 20章新增，用来创建代理对象的工厂接口
 */
@SPI
public interface ProxyFactory {

    //获取代理对象
    <T> T getProxy(Class<T> clazz);

    //默认初始化方法为空
    default <T> void init(ProxyConfig<T> config){}
}
