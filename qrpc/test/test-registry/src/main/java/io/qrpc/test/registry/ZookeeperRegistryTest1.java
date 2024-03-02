package io.qrpc.test.registry;

import io.qrpc.protocol.meta.ServiceMeta;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.registry.zookeeper.ZookeeperRegistryService;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName: ZookeeperRegistryTest
 * @Author: qiuzhiq
 * @Date: 2024/2/22 17:50
 * @Description:
 */

public class ZookeeperRegistryTest1 {
    private RegistryService registryService;
    private ServiceMeta serviceMeta;

    @Before
    public void init() throws Exception {
//        //创建注册配置类RegistryConfig，ZookeeperRegistryService的初始化需要配置类
//        RegistryConfig config = new RegistryConfig("127.0.0.1:2181", "zookeeper");
//        this.registryService = new ZookeeperRegistryService();
//        //根据配置类初始化
//        this.registryService.init(config);
//
//        //服务元数据
//        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest1.class.getName(), "1.0.0", "qiu", "127.0.0.1", 8080);
    }

    @Test
    public void testRegistry() throws Exception {
        this.registryService.registry(this.serviceMeta);
        System.in.read();
    }

    @Test
    public void testUnregistry() throws Exception {
        registryService.unregistry(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception {
//        ServiceMeta meta = registryService.discovery(ZookeeperRegistryTest1.class.getName(), "qiu".hashCode());
//        System.out.println(meta.toString());
    }

    @Test
    public void testDestory() throws Exception {
        registryService.destory(serviceMeta);
    }
}
