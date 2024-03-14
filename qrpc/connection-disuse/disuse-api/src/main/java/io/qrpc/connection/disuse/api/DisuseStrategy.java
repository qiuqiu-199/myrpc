package io.qrpc.connection.disuse.api;

import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.constants.RpcConstants;
import io.qrpc.spi.annotation.SPI;

import java.util.List;

/**
 * @InterfaceName: DisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 10:31
 * @Description: SPI接口，通过SPI机制扩展淘汰策略
 */
@SPI(RpcConstants.CONNECTION_DISUSE_STRATEGY_DEFAULT)
public interface DisuseStrategy {
    /**
     * @author: qiu
     * @date: 2024/3/13 11:29
     * @description: 获取要淘汰的连接
     */
    ConnectionInfo selectConnection(List<ConnectionInfo> connectionList);
}
