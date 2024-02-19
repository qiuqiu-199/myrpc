package io.qrpc.consumer;

import io.qrpc.consumer.common.RpcConsumer;
import io.qrpc.proxy.jdk.JdkProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RpcClient
 * @Author: qiuzhiq
 * @Date: 2024/2/18 17:05
 * @Description:
 */

public class RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String serviceVersion;
    private String serviceGroup;
    private String seriliazationType;
    private long timeout;
    private boolean async;
    private boolean oneway;

    public RpcClient(String serviceVersion, String serviceGroup, String seriliazationType, long timeout, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.seriliazationType = seriliazationType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
    }

    public <T> T create(Class<T> interfaceClass){
        LOGGER.info("RpcClient#create...");
        //多传入一个Consumer对象
        JdkProxyFactory proxyFactory = new JdkProxyFactory(serviceVersion, serviceGroup, seriliazationType, timeout, RpcConsumer.getInstance(), async, oneway);
        return proxyFactory.getProxy(interfaceClass);
    }

    public void shutdown(){
        RpcConsumer.getInstance().close();
    }
}
