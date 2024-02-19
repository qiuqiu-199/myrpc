package io.qrpc.proxy.api.object;

import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.proxy.api.consumer.Consumer;
import io.qrpc.proxy.api.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ObjectProxy
 * @Author: qiuzhiq
 * @Date: 2024/2/18 15:46
 * @Description:
 */

public class ObjectProxy<T> implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    private Class<T> clazz;
    private String serviceVersion;
    private String serviceGroup;
    private long timeout = 15000;
    private Consumer consumer;  //消费者启动端
    private String serializationType;
    private boolean async;
    private boolean oneway;

    //2种构造方法
    //构造方法1：17章暂未用到
    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    //构造方法2：全参构造
    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, long timeout, Consumer consumer, String serializationType, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("ObjectProxy#invoke...");
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

        RpcFuture future = this.consumer.sendRequest(protocol);
        return future == null ? null : timeout > 0 ? future.get(timeout, TimeUnit.MILLISECONDS) : future.get();
    }
}
