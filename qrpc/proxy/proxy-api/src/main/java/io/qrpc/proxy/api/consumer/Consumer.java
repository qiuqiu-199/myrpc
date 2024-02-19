package io.qrpc.proxy.api.consumer;

import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.proxy.api.future.RpcFuture;

/**
 * @InterfaceName: Consumer
 * @Author: qiuzhiq
 * @Date: 2024/2/18 15:37
 * @Description: 动态代理模块新增接口
 */

public interface Consumer {
    RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception;
}
