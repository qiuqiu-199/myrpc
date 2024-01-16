package io.qrpc.common.scanner.service;

import io.qrpc.annotation.RpcService;
import io.qrpc.common.scanner.ClassScanner;
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
     * f
     *
     * @param host        域名
     * @param port        端口
     * @param scanPackage 指定包
     *                    //     * @param registryService
     * @return
     * @throws Exception
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(String host,
                                                                                                int port,
                                                                                                String scanPackage/*,
                                                                                               RegistryService registryService*/) throws Exception {
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
                    //TODO 后续逻辑：向注册中心注册服务员数据，并在handlerMap中缓存...
                    LOGGER.info("当前标注了@RpcService注解的类的名称：" + clazz.getName());
                    LOGGER.info("该类的属性信息：");
                    LOGGER.info("interfaceClass: " + rpcService.interfaceclass().getName());
                    LOGGER.info("interfaceClassName: " + rpcService.interfaceClassName());
                    LOGGER.info("version: " + rpcService.version());
                    LOGGER.info("group: " + rpcService.group());

                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("@rpcService注解扫描出错：", e);
//                e.printStackTrace();
            }
        });
        return handlerMap;

    }

}
