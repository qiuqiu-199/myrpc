package io.qrpc.connection.disuse.defaultStrategy;

import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: DefaultDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 11:32
 * @Description: 默认连接淘汰策略
 */

@SpiClass
public class DefaultDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(DefaultDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：默认淘汰策略...");
        return connectionList.get(0);
    }
}
