package io.qrpc.connection.disuse.lru;

import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: LruDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 22:56
 * @Description: 淘汰上次使用时间离现在太久的
 */
@SpiClass
public class LruDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(LruDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：淘汰最近最少使用的连接...");
        connectionList.sort((c1,c2)->{
            return c1.getLastUseTime() - c2.getLastUseTime() > 0 ? 1 : -1;
        });
        return connectionList.get(0);
    }
}
