package io.qrpc.common.helper;

/**
 * @ClassName: RpcServiceHelper
 * @Author: qiuzhiq
 * @Date: 2024/1/19 12:20
 * @Description: 提供者的工具类
 */

public class RpcServiceHelper {

    /**
     * f
     *
     * @param serviceName    服务名
     * @param serviceVersion 服务版本
     * @param group          服务分组
     * @return 三个部分用井号拼接的字符串
     */
    public static String buildServiceKey(String serviceName, String serviceVersion, String group) {
        return String.join("#", serviceName, serviceVersion, group);
    }
}
