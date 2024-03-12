package io.qrpc.demo.spring.annotation.provider.config;

import io.qrpc.provider.spring.RpcSpringServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @ClassName: ProviderConfig
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:24
 * @Description: 配置类，根据配置文件构建服务端实例注入容器中
 */
@Configuration
@ComponentScan(value = {"io.qrpc.demo"})
//@ComponentScan(value = {"io.qrpc.demo.spring.annotation.provider.Impl"})//同效
@PropertySource(value = {"classpath:rpc.properties"})
public class ProviderConfig {

    @Value("${registry.type}")
    private String registryType;
    @Value("${registry.address}")
    private String registryAddr;
    @Value("${registry.loadbalance.type}")
    private String registryLoadbalance;

    @Value("${server.address}")
    private String serverAddr;
    @Value("${reflect.type}")
    private String reflectType;

    @Value("${server.heartbeatInterval}")
    private int heatbeatInterval;
    @Value("${server.scanNotActiveChannelInterval}")
    private int scanNotActiveChannelInterval;

    @Value("${cacheResult.enable}")
    private boolean enableCacheResult;
    @Value("${cacheResult.expire}")
    private int cacheResultExpire;

    @Bean
    public RpcSpringServer rpcSpringServer(){
        return new RpcSpringServer(serverAddr,registryType,registryAddr,registryLoadbalance,reflectType,heatbeatInterval,scanNotActiveChannelInterval,enableCacheResult,cacheResultExpire);
    }
}
