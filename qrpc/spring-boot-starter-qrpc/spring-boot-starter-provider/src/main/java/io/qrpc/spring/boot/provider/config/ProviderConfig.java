package io.qrpc.spring.boot.provider.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProviderConfig
 * @Author: qiuzhiq
 * @Date: 2024/3/9 11:31
 * @Description: 参数类，在自动配置类中通过yml配置文件获取参数，并注入容器中
 */
@Data
@NoArgsConstructor
public class ProviderConfig {

    private String serverAddr;

    private String registryType;
    private String registryAddr;
    private String registryLoadBalanceType;

    private String reflectType;

    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;

    public ProviderConfig(String serverAddr, String registryType, String registryAddr, String registryLoadBalanceType, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval) {
        this.serverAddr = serverAddr;

        this.registryType = registryType;
        this.registryAddr = registryAddr;
        this.registryLoadBalanceType = registryLoadBalanceType;

        this.reflectType = reflectType;

        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }
}
