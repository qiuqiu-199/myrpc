package io.qrpc.connection.manager;

import io.netty.channel.Channel;
import io.qrpc.common.exception.RefuseException;
import io.qrpc.connection.disuse.api.DisuseStrategy;
import io.qrpc.connection.disuse.api.connection.ConnectionInfo;
import io.qrpc.constants.RpcConstants;
import io.qrpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: ConnectionManager
 * @Author: qiuzhiq
 * @Date: 2024/3/13 12:17
 * @Description: 连接管理器，单例，创建连接、使用连接、关闭连接时都会触发
 */

public class ConnectionManager {
    //保存连接信息，key为channel的id
    private volatile Map<String, ConnectionInfo> connMap = new ConcurrentHashMap<>();
    //连接淘汰策略
    private final DisuseStrategy disuseStrategy;
    //最大连接数
    private final int maxConnectionCount;
    //单例
    private static volatile ConnectionManager instance;


    //根据用户定义的最大连接数与连接淘汰策略构造连接管理器
    private ConnectionManager(int maxConnectionCount, String disuseStrategyType) {
        this.maxConnectionCount = maxConnectionCount <= 0 ? Integer.MAX_VALUE : maxConnectionCount;

        disuseStrategyType = StringUtils.isEmpty(disuseStrategyType) ? RpcConstants.CONNECTION_DISUSE_STRATEGY_DEFAULT : disuseStrategyType;

        this.disuseStrategy = ExtensionLoader.getExtension(DisuseStrategy.class, disuseStrategyType);
    }

    //单例方法
    public static ConnectionManager getInstance(int maxConnectionCount, String disuseStrategyType) {
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager(maxConnectionCount, disuseStrategyType);
                }
            }
        }
        return instance;
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 12:51
     * @description: 添加新的连接，如果连接数满了就淘汰连接
     */
    public void add(Channel channel) {
        ConnectionInfo connectionInfo = new ConnectionInfo(channel);
        if (this.checkConnectionList(connectionInfo)) {
            connMap.put(getKey(channel), connectionInfo);
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 12:52
     * @description: 移除连接
     */
    public void remove(Channel channel) {
        connMap.remove(getKey(channel));
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 12:52
     * @description: 使用连接时更新连接信息
     */
    public void update(Channel channel) {
        ConnectionInfo connectionInfo = connMap.get(getKey(channel));
        connectionInfo.setLastUseTime(System.currentTimeMillis());
        connectionInfo.incrementUseCount();
        connMap.put(getKey(channel), connectionInfo);
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 12:52
     * @description: 检查连接数是否达到上限，是则淘汰连接
     */
    private boolean checkConnectionList(ConnectionInfo connectionInfo) {
        //这里会按照put顺序转换
        ArrayList<ConnectionInfo> infoList = new ArrayList<>(connMap.values());
        if (infoList.size() == 0) return true;

        if (infoList.size() >= maxConnectionCount) {
            try {
                ConnectionInfo connectionInfo_toRemove = disuseStrategy.selectConnection(infoList);
                if (connectionInfo_toRemove != null) {
                    connectionInfo_toRemove.getChannel().close();
                    connMap.remove(getKey(connectionInfo_toRemove.getChannel()));
                }
            } catch (RefuseException e) {
                connectionInfo.getChannel().close();
                return false;
            }
        }
        return true;
    }

    /**
     * @author: qiu
     * @date: 2024/3/13 12:53
     * @description: channel对应的key为id
     */
    private String getKey(Channel channel) {
        return channel.id().asLongText();
    }
}
