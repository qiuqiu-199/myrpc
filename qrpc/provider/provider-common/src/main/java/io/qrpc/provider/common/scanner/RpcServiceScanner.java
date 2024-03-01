package io.qrpc.provider.common.scanner;

import io.qrpc.annotation.RpcService;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.common.scanner.ClassScanner;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RpcServiceScanner
 * @Author: qiuzhiq
 * @Date: 2024/1/16 9:46
 * @description: 扫描@RpcService注解并缓存对应的类
 */

public class RpcServiceScanner extends ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * @param host            域名
     * @param port            端口
     * @param scanPackage     指定包
     * @param registryService 注册中心
     * @return Map<String, Object>
     * @throws Exception
     * @Descreption: 22章修改，引入注册中心，扫描到RPCService注解后将服务注册到注册中心
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(
            String scanPackage,
            String host,
            int port,
            RegistryService registryService) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) return handlerMap;

//        classNameList.stream().forEach((className)->{
        classNameList.forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    //优先使用interfaceClass，如果它的name为null，再使用interfaceClassName
                    //22章修改，引入注册中心后，构建服务元数据并将服务注册到注册中心
                    ServiceMeta serviceMeta = new ServiceMeta(
                            getServiceName(rpcService),
                            rpcService.version(),
                            rpcService.group(),
                            host,
                            port,
                            getweight(rpcService.weight())
                    );
                    registryService.registry(serviceMeta);

                    //TODO 目前先简单处理key为服务名+版本+分组，后续完善
                    String key = RpcServiceHelper.buildServiceKey(rpcService.interfaceClassName(), rpcService.version(), rpcService.group());
                    handlerMap.put(key, clazz.newInstance());
                }
            } catch (Exception e) {
                LOGGER.error("@rpcService注解扫描出错：", e);
            }
        });
        return handlerMap;
    }

    /**
     * @author: qiu
     * @date: 2024/3/1 14:27
     * @param: weight
     * @return: int
     * @description: 6.5节新增，让用户设定的权重不超过上下限
     */
    private static int getweight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN)
            return RpcConstants.SERVICE_WEIGHT_MIN;
        else if (weight > RpcConstants.SERVICE_WEIGHT_MAX)
            return RpcConstants.SERVICE_WEIGHT_MAX;
        return weight;
    }

    /**
     * @author: qiu
     * @date: 2024/2/24 11:39
     * @param: rpcService
     * @return: java.lang.String
     * @description: 22章新增，抽出一个方法来，根据注解RPCService获取服务名，优先使用interfaceClass.getName()，如果没有就使用interfaceClassName
     */
    private static String getServiceName(RpcService rpcService) {
        Class<?> clazz = rpcService.interfaceClass();
        if (clazz == void.class) return rpcService.interfaceClassName();
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty())
            serviceName = rpcService.interfaceClassName();
        return serviceName;
    }
}
