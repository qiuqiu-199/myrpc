package io.qrpc.connection.disuse.api.connection;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: ConnectionInfo
 * @Author: qiuzhiq
 * @Date: 2024/3/13 10:13
 * @Description: 封装连接信息
 */
@NoArgsConstructor
public class ConnectionInfo implements Serializable {
    private static final long serialVersionUID = -569204447533050888L;

    //连接channel
    @Setter
    @Getter
    private Channel channel;
    //连接创建时间
    @Setter
    @Getter
    private long connectionTime;
    //连接上次使用时间
    @Setter
    @Getter
    private long lastUseTime;
    //连接使用次数
    private AtomicInteger useCount = new AtomicInteger(0);

    //构造方法
    public ConnectionInfo(Channel channel) {
        this.channel = channel;
        this.connectionTime = System.currentTimeMillis();
        this.lastUseTime = connectionTime;
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 10:28
     * @description: channel相同可以认定两个connectionInfo相同
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConnectionInfo connectionInfo = (ConnectionInfo) obj;
        return Objects.equals(channel, connectionInfo.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 10:21
     * @description: 获取连接使用次数
     */
    public int getUseCount(){
        return useCount.get();
    }
    /**
     * @author: qiu
     * @date: 2024/3/13 10:28
     * @description: 增加连接使用次数
     */
    public int incrementUseCount(){
        return this.useCount.incrementAndGet();
    }
}
