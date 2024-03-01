package io.qrpc.loadBalancer.context;

import io.qrpc.protocol.meta.ServiceMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: ConnectionsContext
 * @Author: qiuzhiq
 * @Date: 2024/3/1 19:45
 * @Description: 6.5节，扩展最少连接数的负载策略
 */

public class ConnectionsContext {
    private static volatile Map<String,Integer> connectionMap = new ConcurrentHashMap<>();

    /**
     * @author: qiu
     * @date: 2024/3/1 20:29
     * @param: serviceMeta
     * @return: void
     * @description: 建立连接时，服务对应的连接数+1
     */
    public static void addConnection(ServiceMeta serviceMeta){
        String servicekey = String.join(":",serviceMeta.getServiceAddr(),String.valueOf(serviceMeta.getServicePort()));
        Integer connectionCount = connectionMap.get(servicekey);
        if (connectionCount == null){
            connectionCount = 0;
        }
        connectionCount++;
        connectionMap.put(servicekey,connectionCount);
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 20:30
     * @param: serviceMeta
     * @return: java.lang.Integer
     * @description: 获取服务对应的连接数
     */
    public static Integer getConnection(ServiceMeta serviceMeta){
        String serviceKey = String.join(":",serviceMeta.getServiceAddr(),String.valueOf(serviceMeta.getServicePort()));
        return connectionMap.get(serviceKey);
    }
}
