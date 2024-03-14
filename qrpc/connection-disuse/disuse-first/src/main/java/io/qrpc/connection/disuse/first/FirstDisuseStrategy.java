package io.qrpc.connection.disuse.first;

import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: FirstDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 20:40
 * @Description: 淘汰最早建立的连接
 */
@SpiClass
public class FirstDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(FirstDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：淘汰最早建立的连接...");
        //按照连接建立时间从早到晚排序，淘汰最早建立的连接
        connectionList.sort((c1, c2) -> {
            return c1.getConnectionTime() - c2.getConnectionTime() > 0 ? 1 : -1;
        });
        return connectionList.get(0);
    }
}
