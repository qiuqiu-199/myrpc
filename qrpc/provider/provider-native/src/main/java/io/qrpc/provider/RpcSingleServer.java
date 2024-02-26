package io.qrpc.provider;

import io.qrpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.qrpc.provider.common.scanner.RpcServiceScanner;

/**
 * @ClassName: RpcSingleServer
 * @Author: qiuzhiq
 * @Date: 2024/1/17 11:00
 * @Description: RpcSingleServer类作为使用Java方式，不依赖spring启动rpc框架的类
 */

public class RpcSingleServer extends BaseServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress, String scanPackage,String registryAddress,String registryType,String reflectType) {
        //TODO 这里必须调用父类构造方法否则报错，原因不明
        super(serverAddress,registryAddress,registryType,reflectType);

        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage,this.host,this.port,this.registryService);
        } catch (Exception e) {
            LOGGER.error("RPC Server init error",e);
        }
    }
}
