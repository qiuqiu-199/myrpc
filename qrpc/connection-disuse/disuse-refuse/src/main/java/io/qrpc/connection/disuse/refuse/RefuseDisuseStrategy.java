package io.qrpc.connection.disuse.refuse;

import io.qrpc.common.exception.RefuseException;
import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.spi.annotation.SpiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName: RefuseDisuseStrategy
 * @Author: qiuzhiq
 * @Date: 2024/3/13 23:01
 * @Description: 拒绝新连接
 */
@SpiClass
public class RefuseDisuseStrategy implements DisuseStrategy {
    private final static Logger log = LoggerFactory.getLogger(RefuseDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("淘汰策略：拒绝新连接...");
        throw new RefuseException("当前提供者拒绝新连接！");
    }
}
