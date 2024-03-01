package io.qrpc.consumer.common.helper;

import io.qrpc.consumer.common.handler.RpcConsumerHandler;
import io.qrpc.protocol.meta.ServiceMeta;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: RpcConsumerHandlerHelper
 * @Author: qiuzhiq
 * @Date: 2024/2/25 11:16
 * @Description: 23章新增，RpcConsumer创建的Handler放在这里的map中
 */

public class RpcConsumerHandlerHelper {
    private static Map<String, RpcConsumerHandler> handlerMap;

    static {
        handlerMap = new ConcurrentHashMap<>();
    }

    /**
     * @author: qiu
     * @date: 2024/2/26 9:16
     * @param: serviceMeta
     * @return: java.lang.String
     * @description: 23章新增，生成map的key，ip_port
     */
    private static String getKey(ServiceMeta serviceMeta) {
        return String.join("_",
                serviceMeta.getServiceAddr(),
                String.valueOf(serviceMeta.getServicePort()));
    }

    /**
     * @author: qiu
     * @date: 2024/2/26 9:16
     * @param: serviceMeta
     * @param: handler
     * @return: void
     * @description: 23章新增，存入ConsumerHandler
     */
    public static void put(ServiceMeta serviceMeta,RpcConsumerHandler handler){
        handlerMap.put(getKey(serviceMeta),handler);
    }
    /**
     * @author: qiu
     * @date: 2024/2/26 9:17
     * @param: serviceMeta
     * @return: io.qrpc.consumer.common.handler.RpcConsumerHandler
     * @description: 23章新增，根据key获取ConsumerHandler
     */
    public static RpcConsumerHandler get(ServiceMeta serviceMeta){
        return handlerMap.get(getKey(serviceMeta));
    }

    /**
     * @author: qiu
     * @date: 2024/2/26 9:17
     * @param:
     * @return: void
     * @description: map是静态的，存储的ConsumerHandler在关闭消费者时要及时close
     */
    public static void closeRpcClientHandler(){
        Collection<RpcConsumerHandler> handlers = handlerMap.values();
        if (handlers != null){
            handlers.forEach((handler)->{
                handler.close();
            });
        }
        handlers.clear();  //TODO 待进一步理解
    }
}
