package io.qrpc.proxy.api.object;

import io.qrpc.cache.result.CacheResultKey;
import io.qrpc.cache.result.CacheResultManager;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.reflect.api.ReflectInvoker;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ObjectProxy
 * @Author: qiuzhiq
 * @Date: 2024/2/18 15:46
 * @Description: 18章新增
 */

public class ObjectProxy<T> implements IAsyncObjectProxy, InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    private Class<T> clazz;
    private String serviceVersion;
    private String serviceGroup;
    private long timeout = 15000;
    private Consumer consumer;  //消费者启动端
    private String serializationType;
    private boolean async;
    private boolean oneway;
    private RegistryService registryService;
    private boolean enableCacheResult;
    private CacheResultManager<Object> cacheResultManager;
    //容错层
    private ReflectInvoker reflectInvoker;
    private Class<?> fallbackClass;


    //2种构造方法
    //构造方法1：17章暂未用到
    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    //构造方法2：全参构造
    public ObjectProxy(
            Class<T> clazz,
            String serviceVersion,
            String serviceGroup,
            String serializationType,
            long timeout,
            Consumer consumer,
            boolean async,
            boolean oneway,
            RegistryService registryService,
            boolean enableCacheResult,
            int cacheResultExpire,
            String reflectType,
            String fallbackClassName,
            Class<?> fallbackClass
    ) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;

        this.enableCacheResult = enableCacheResult;
        this.cacheResultManager = CacheResultManager.getInstance(enableCacheResult,cacheResultExpire);

        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class,reflectType);
        this.fallbackClass = this.getFallbackClass(fallbackClassName,fallbackClass);
    }

    /**
     * @author: qiu
     * @date: 2024/3/14 11:23
     * @description: 获取容错处理类，优先从fallbackClass获取，为空再根据fallbackClassName获取
     */
    private Class<?> getFallbackClass(String fallbackClassName, Class<?> fallbackClass) {
        if (isFallbackClassEmpty(fallbackClass)){
            try {
                if (!StringUtils.isEmpty(fallbackClassName)){
                    fallbackClass = Class.forName(fallbackClassName);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("获取容错处理类时出错！{}",e.getMessage());
            }
        }
        return fallbackClass;
    }

    /**
     * @author: qiu
     * @date: 2024/3/14 11:24
     * @description: 判断容错类是否为空，void.class也为空
     */
    private boolean isFallbackClassEmpty(Class<?> fallbackClass) {
        return fallbackClass == null
                || fallbackClass == RpcConstants.FALLBACK_CLASS_DEFAULT
                || RpcConstants.FALLBACK_CLASS_DEFAULT.equals(fallbackClass);
    }

    /**
     * @author: qiu
     * @date: 2024/2/26 9:45
     * @description: 代理方法，封装请求协议对象并发送请求、接收结果
     * 23章，调用sendRequest增加参数RegistryService
     * 11节，增加缓存层
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("ObjectProxy#invoke动态代理的同步调用...");
        //invoke方法里对三种方法做一个通用的特殊处理，
        // equals方法直接返回proxy和args[0]的比较结果，
        // hashcode方法返回proxy的哈希值，
        // toString返回我们自定义的字符串信息
        if (Object.class.equals(method.getDeclaringClass())) {
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            } else if ("hashcode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(method.getName())) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler" + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        //如果开启了缓存，先从缓存获取响应
        if (enableCacheResult)
            return invokeSendRequestMethodCache(method,args);
        return  invokeSendRequestMethod(method,args);
    }

    /**
     * @author: qiu
     * @date: 2024/3/12 16:25
     * @description: 从缓存中获取响应，如果没有直接发送请求
     */
    private Object invokeSendRequestMethodCache(Method method, Object[] args) throws Exception {
        LOGGER.info("从缓存中获取响应中...");
        CacheResultKey cacheResultKey = new CacheResultKey(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, serviceVersion, serviceGroup);
        Object obj = this.cacheResultManager.get(cacheResultKey);
        if (obj == null){
            LOGGER.info("未缓存响应，发送请求中...");
            obj = invokeSendRequestMethod(method,args);
            if (obj != null){
                cacheResultKey.setCacheTimeStamp(System.currentTimeMillis());
                this.cacheResultManager.put(cacheResultKey,obj);
            }
        }
        return obj;
    }

    /**
     * @author: qiu
     * @date: 2024/3/12 16:26
     * @description: 发送请求并接受请求
     * 13节增加容错层
     */
    private Object invokeSendRequestMethod(Method method, Object[] args) throws Exception {
        try {
            //封装协议信息并发送
            RpcProtocol<RpcRequest> protocol = wrapRequestProtocol(method,args);
            RpcFuture future = this.consumer.sendRequest(protocol,registryService);
            return future == null ? null : timeout > 0 ? future.get(timeout, TimeUnit.MILLISECONDS) : future.get();
        } catch (Exception e) {
//            e.printStackTrace();  //debug时可以打印信息
            if (isFallbackClassEmpty(fallbackClass))
                return null;
            else
                return getFallbackResult(method,args);
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/14 15:34
     * @description: 封装请求协议
     */
    private RpcProtocol<RpcRequest> wrapRequestProtocol(Method method, Object[] args) {
        //封装协议信息
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        //协议头
        protocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType, RpcType.REQUEST.getType()));
        //协议体
        RpcRequest requestBody = new RpcRequest();
        requestBody.setClassName(method.getDeclaringClass().getName());
        requestBody.setMethodName(method.getName());
        requestBody.setParameterTypes(method.getParameterTypes());
        requestBody.setParameters(args);
        requestBody.setVersion(this.serviceVersion);
        requestBody.setGroup(this.serviceGroup);
        requestBody.setAsync(this.async);
        requestBody.setOneway(this.oneway);
        protocol.setBody(requestBody);

        //信息输出
        LOGGER.warn("代理信息======start");
        LOGGER.warn("invoke。。。代理方法所在类：{}", method.getDeclaringClass().getName());
        LOGGER.warn("invoke。。。代理方法名：{}", method.getName());
        //TODO 这里看看能不能消掉
        if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            LOGGER.warn("invoke。。。代理方法参数类型及对应值:");
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                LOGGER.warn(method.getParameterTypes()[i].getName() + "--->" + args[i]);
            }
        }
        LOGGER.warn("代理信息======end");

        return protocol;
    }

    //获取容错结果
    private Object getFallbackResult(Method method, Object[] args) {
        try {
            return reflectInvoker.invokeMethod(fallbackClass.newInstance(),fallbackClass,method.getName(),method.getParameterTypes(),args);
        } catch (Throwable throwable) {
            LOGGER.error("容错结果获取出错！" + throwable.getMessage());
        }
        return null;
    }


    /**
     * @author: qiu
     * @date: 2024/2/21 16:38
     * @description: 19章，由异步化调用对象调用，根据传入方法名及参数调用远程方法
     * 23章，调用sendRequest增加参数RegistryService
     */
    @Override
    public RpcFuture call(String funName, Object... args) {
        LOGGER.warn("ObjectProxy#call动态代理的异步调用...");
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funName, args);
        RpcFuture future = null;
        try {
            future = this.consumer.sendRequest(request,registryService);
        } catch (Exception e) {
            LOGGER.error("动态代理的异步调用过程中出错：{}", e);
        }
        return future;
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 16:38
     * @description: 19章，根据接口名、方法名及方法参数创建请求协议对象
     */
    private RpcProtocol<RpcRequest> createRequest(String className, String funName, Object[] args) {
        LOGGER.info("ObjectProxy#createRequest...");
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader(this.serializationType,RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(funName);
        Class[] paramterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(paramterTypes);
        request.setParameters(args);
        request.setVersion(this.serviceVersion);
        request.setGroup(this.serviceGroup);
        request.setOneway(this.oneway);
        request.setAsync(this.async);

        protocol.setBody(request);
        return protocol;
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 16:38
     * @description: 19章，根据传入的参数，返回参数对应的Class对象
     */
    private Class getClassType(Object arg) {
        Class<?> clazz = arg.getClass();
        String typeName = clazz.getName();
        //基本数据类型特殊处理
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
        }
        return clazz;
    }
}
