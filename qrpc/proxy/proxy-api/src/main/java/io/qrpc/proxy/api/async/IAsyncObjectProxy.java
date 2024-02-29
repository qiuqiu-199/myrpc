package io.qrpc.proxy.api.async;

import io.qrpc.proxy.api.future.RpcFuture;

/**
 * @InterfaceName: IAsyncObjectProxy
 * @Author: qiuzhiq
 * @Date: 2024/2/21 15:36
 * @Description: 19章新增
 */

public interface IAsyncObjectProxy {
    /**
     * @author: qiu
     * @date: 2024/2/21 16:15
     * @param: funName
     * @param: args
     * @return: io.qrpc.objectProxy.api.future.RpcFuture
     * @description:
     */
    RpcFuture call(String funName, Object... args);
}
