package io.qrpc.provider.common.manager;

import io.netty.channel.Channel;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.response.RpcResponse;
import io.qrpc.provider.common.cache.ProviderConnectionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @ClassName: ProviderConnectionManager
 * @Author: qiuzhiq
 * @Date: 2024/3/3 23:27
 * @Description:
 */

public class ProviderConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ProviderConnectionManager.class);

    /**
     * @author: qiu
     * @date: 2024/3/3 22:31
     * @description: 移除不活跃的channel
     */
    public static void removeNotActiveChannel(){
        Set<Channel> channels = ProviderConnectionCache.getChannelCache();

        if (channels == null || channels.isEmpty()) return;

        channels.forEach((channel -> {
            if (!channel.isOpen() || !channel.isActive()){
                channel.close();
                ProviderConnectionCache.removeChannel(channel);
            }
        }));
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 22:46
     * @description: 7节新增，向所有连接成功的消费者发送ping消息
     */
    public static void sendPingFromProvider(){
        Set<Channel> channels = ProviderConnectionCache.getChannelCache();
        if (channels == null || channels.isEmpty()) return;

        //构建请求协议对象，封装ping消息
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        //消息头
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_PROVIDER.getType());
        //消息体
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEARTBEAT_PING);
        //封装
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);

        //每个channel发送一次ping消息
        channels.forEach((channel -> {
            if (channel.isOpen() && channel.isActive()){
                log.info("提供者者{}向消费者{}发送ping消息，消息内容为：{}",channel.localAddress(),channel.remoteAddress(),RpcConstants.HEARTBEAT_PING);
            }
            //发送
            channel.writeAndFlush(responseRpcProtocol);
        }));
    }
}
