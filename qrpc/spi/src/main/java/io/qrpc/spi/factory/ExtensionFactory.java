package io.qrpc.spi.factory;

import io.qrpc.spi.annotation.SPI;

/**
 * @InterfaceName: ExtensionFactory
 * @Author: qiuzhiq
 * @Date: 2024/2/27 14:19
 * @Description: 25章新增，目前暂不清楚作用
 */
@SPI("spi")
public interface ExtensionFactory {
    /**
     * @author: qiu
     * @date: 2024/2/27 14:21
     * @param: key key值
     * @param: clazz Class对象
     * @return: T
     * @description: 根据传入的key和Class对象获取Class对应的扩展类对象
     */
    <T> T getExtension(String key, Class<T> clazz);
}
