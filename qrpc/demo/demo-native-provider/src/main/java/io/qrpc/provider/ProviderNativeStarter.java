package io.qrpc.provider;

import org.junit.Test;

/**
 * @ClassName: ProviderNativeStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/6 18:48
 * @Description:
 */

public class ProviderNativeStarter {
    @Test
    public void startRpcProvider_native(){
        RpcSingleServer server = new RpcSingleServer(
                "127.0.0.1:27880",
                "io.qrpc.demo",
                "nacos",
                "127.0.0.1:8848",
                "random",
                "jdk",
                -1,
                -1,
                true,
                6000,
                1,
                "strategy_default"
        );
        server.startNettyServer();
    }
}
