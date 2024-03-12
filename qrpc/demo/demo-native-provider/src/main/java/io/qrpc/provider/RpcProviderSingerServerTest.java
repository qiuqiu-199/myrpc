package io.qrpc.provider;

import org.junit.Test;

/**
 * @ClassName: RpcProviderSingerServerTest
 * @Author: qiuzhiq
 * @Date: 2024/3/6 18:48
 * @Description:
 */

public class RpcProviderSingerServerTest {
    @Test
    public void startRpcProvider_native(){
        RpcSingleServer server = new RpcSingleServer("127.0.0.1:27880","io.qrpc.test","127.0.0.1:8848","nacos","random","jdk",-1,-1,true,6000);
        server.startNettyServer();
    }
}
