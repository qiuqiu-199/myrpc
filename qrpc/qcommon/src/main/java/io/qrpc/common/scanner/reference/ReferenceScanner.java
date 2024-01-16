package io.qrpc.common.scanner.reference;

import io.qrpc.annotation.RpcReference;
import io.qrpc.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @ClassName: ReferenceScanner
 * @Author: qiuzhiq
 * @Date: 2024/1/16 15:25
 * @Description:
 */

public class ReferenceScanner extends ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceScanner.class);

    /**f
     * 扫描所有类中被@RpcReference注解标注的字段准备生成和注入代理对象
     * @param packageName
     * @return
     * @throws Exception
     */
    public static Map<String,Object> doScannerWithReferenceAnnotationFilter(String packageName) throws Exception {
        Map<String,Object> handlerMap = new HashMap<>();  //缓存接口引用的代理对象
        List<String> classNameList = getClassNameList(packageName);  //通过通用扫描器扫描所有文件信息
        if (classNameList == null || classNameList.isEmpty()) return handlerMap;
        //遍历扫描的所有类信息的字段，找到被注解标注的字段，准备生成代理对象。这里暂时
        classNameList.forEach((className)->{
            try {
                Class<?> clazz = Class.forName(className);
                Field[] declaredFields = clazz.getDeclaredFields();
                Stream.of(declaredFields).forEach((field -> {
                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                    if (rpcReference != null){
                        //TODO 后续处理逻辑：将@Reference注解标注的接口引用代理对象放入全局缓存
                        LOGGER.info("当前标注了@RpcReference注解的字段名称===>>> " + field.getName());
                        LOGGER.info("@RpcReference注解上标注的属性信息如下：");
                        LOGGER.info("version===>>> " + rpcReference.version());
                        LOGGER.info("group===>>> " + rpcReference.group());
                        LOGGER.info("registryType===>>> " + rpcReference.registryType());
                        LOGGER.info("registryAddress===>>> " + rpcReference.registryAddress());

                    }
                }));
            } catch (ClassNotFoundException e) {
                LOGGER.error("scan classes throws exception: {}", e);
//                e.printStackTrace();
            }
        });
        return handlerMap;
    }
}
