package io.qrpc.proxy.api.object;

import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.proxy.api.async.IAsyncObjectProxy;
import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.proxy.api.future.RpcFuture;
import io.qrpc.registry.api.RegistryService;
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

    //2种构造方法
    //构造方法1：17章暂未用到
    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    //构造方法2：全参构造
    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, String serializationType, long timeout, Consumer consumer, boolean async, boolean oneway,RegistryService registryService) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
    }

    /**
     * @author: qiu
     * @date: 2024/2/26 9:45
     * @param: objectProxy
     * @param: method
     * @param: args
     * @return: java.lang.Object
     * @description: 代理方法，封装请求协议对象并发送请求、接收结果
     * 23章，调用sendRequest增加参数RegistryService
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("ObjectProxy#invoke动态代理的同步调用...");
        //TODO 待进一步理解
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

        //封装协议信息
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType));
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

        //协议信息输出
        LOGGER.info("代理信息======start");
        LOGGER.info("invoke。。。代理方法所在类：{}", method.getDeclaringClass().getName());
        LOGGER.info("invoke。。。代理方法名：{}", method.getName());
        //TODO 这里看看能不能消掉
        if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            LOGGER.info("invoke。。。代理方法参数类型及对应值:");
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                LOGGER.info(method.getParameterTypes()[i].getName() + "--->" + args[i]);
            }
        }
        LOGGER.info("代理信息======end");

        RpcFuture future = this.consumer.sendRequest(protocol,registryService);
        return future == null ? null : timeout > 0 ? future.get(timeout, TimeUnit.MILLISECONDS) : future.get();
    }

    /**
     * @author: qiu
     * @date: 2024/2/21 16:38
     * @param: funName
     * @param: args
     * @return: io.qrpc.objectProxy.api.future.RpcFuture
     * @description: 19章，由异步化调用对象调用，根据传入方法名及参数调用远程方法
     * 23章，调用sendRequest增加参数RegistryService
     */
    @Override
    public RpcFuture call(String funName, Object... args) {
        LOGGER.info("ObjectProxy#call动态代理的异步调用...");
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
     * @param: className
     * @param: funName
     * @param: args
     * @return: io.qrpc.protocol.RpcProtocol<io.qrpc.protocol.request.RpcRequest>
     * @description: 19章，根据接口名、方法名及方法参数创建请求协议对象
     */
    private RpcProtocol<RpcRequest> createRequest(String className, String funName, Object[] args) {
        LOGGER.info("ObjectProxy#createRequest...");
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader(this.serializationType));
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
     * @param: arg
     * @return: java.lang.Class
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
