package io.qrpc.connection.disuse.random;

import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @ClassName: RandomDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 23:09
 * @Description: 随机淘汰策略
 */
@SpiClass
public class RandomDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(RandomDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：随机淘汰...");
        return connectionList.get(new Random().nextInt(connectionList.size()));
    }
}
