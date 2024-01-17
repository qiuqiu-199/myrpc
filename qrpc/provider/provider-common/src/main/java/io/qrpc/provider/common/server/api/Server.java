package io.qrpc.provider.common.server.api;

/**
 * @InterfaceName: Server
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:39
 * @Description: 启动rpc服务提供者的核心接口
 */

public interface Server {
    void startNettyServer();
}
