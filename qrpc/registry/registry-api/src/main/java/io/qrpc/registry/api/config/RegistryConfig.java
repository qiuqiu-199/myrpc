package io.qrpc.registry.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: RegistryConfig
 * @Author: qiuzhiq
 * @Date: 2024/2/22 10:29
 * @Description: 21章 用于配置注册中心的配置类
 * 42章，添加一个负载均衡变量
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryConfig implements Serializable {
    private static final long serialVersionUID = -8322030800779617018L;
    private String registryAddr;//注册中心的ip
    private String registryType;//注册中心的端口  以Zookeeper为例，本地启动Zookeeper后，应当是127.0.0.0:2181
    private String serviceLoadBalance;
}
