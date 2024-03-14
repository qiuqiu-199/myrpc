package io.qrpc.connection.disuse.lfu;

import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: LfuDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 22:49
 * @Description: 使用次数最少的
 */
@SpiClass
public class LfuDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(LfuDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：淘汰最近最不常使用的连接...");
        connectionList.sort((c1,c2)->{
            return c1.getUseCount() - c2.getUseCount() > 0 ? 1 : -1;
        });
        return connectionList.get(0);
    }
}
