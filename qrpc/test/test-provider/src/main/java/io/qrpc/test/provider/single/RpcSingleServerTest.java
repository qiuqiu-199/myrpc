package io.qrpc.test.provider.single;

import io.qrpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @ClassName: RpcSingleServerTest
 * @Author: qiuzhiq
 * @Date: 2024/1/17 11:58
 * @Description: 测试服务提供者的启动类
 */

public class RpcSingleServerTest {
    @Test
    public void startRpcProvider(){
        RpcSingleServer server = new RpcSingleServer("127.0.0.1:27880","io.qrpc.test");
        server.startNettyServer();
    }
}
