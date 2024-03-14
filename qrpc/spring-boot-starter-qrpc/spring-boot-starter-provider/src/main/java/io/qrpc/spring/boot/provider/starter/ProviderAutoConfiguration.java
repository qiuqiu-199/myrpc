package io.qrpc.spring.boot.provider.starter;

import io.qrpc.provider.spring.RpcSpringServer;
import io.qrpc.spring.boot.provider.config.ProviderConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ProviderAutoConfiguration
 * @Author: qiuzhiq
 * @Date: 2024/3/9 11:33
 * @Description: 提供者端的自动配置类，自动根据yml配置文件配置参数类、根据参数类注入RpcSpringServer组件
 */
@Configuration
@EnableConfigurationProperties //开启自动配置功能
public class ProviderAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "qrpc.provider")//从配置文件中获取参数，prefix是前缀
    public ProviderConfig providerConfig(){
        return new ProviderConfig();
    }

    @Bean
    public RpcSpringServer rpcSpringServer(final ProviderConfig config){
        return new RpcSpringServer(
                config.getServerAddr(),
                config.getRegistryType(),
                config.getRegistryAddr(),
                config.getRegistryLoadBalanceType(),
                config.getReflectType(),
                config.getHeartbeatInterval(),
                config.getScanNotActiveChannelInterval(),
                config.isEnableCacheResult(),
                config.getCacheResultExpire(),
                config.getMaxConnectionCount(),
                config.getDisuseStrategyType()
        );
    }
}
