package io.qrpc.proxy.api.callback;

/**
 * @InterfaceName: AsyncRpcCallback
 * @Author: qiuzhiq
 * @Date: 2024/2/17 10:49
 * @Description: 回调接口，3.5节新增
 */

public interface AsyncRpcCallback {
    /**
     * 成功后的回调方法
     */
    void onSuccess(Object res);
    /**
     * 失败后的回调方法
     */
    void onException(Exception e);
}
