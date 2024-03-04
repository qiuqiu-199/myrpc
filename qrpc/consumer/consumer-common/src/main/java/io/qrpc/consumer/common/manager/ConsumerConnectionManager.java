package io.qrpc.consumer.common.manager;

import io.netty.channel.Channel;
import io.qrpc.constants.RpcConstants;
import io.qrpc.consumer.common.cache.ConsumerChannelCache;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.header.RpcHeaderFactory;
import io.qrpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @ClassName: ConsumerConnectionManager
 * @Author: qiuzhiq
 * @Date: 2024/3/3 22:26
 * @Description: 7节新增，用于管理消费者拥有的channel
 */

public class ConsumerConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConsumerConnectionManager.class);

    /**
     * @author: qiu
     * @date: 2024/3/3 22:31
     * @description: 移除不活跃的channel
     */
    public static void removeNotActiveChannel(){
        Set<Channel> channels = ConsumerChannelCache.getChannelCache();

        if (channels == null || channels.isEmpty()) return;

        channels.forEach((channel -> {
            if (!channel.isOpen() || !channel.isActive()){
                channel.close();
                ConsumerChannelCache.removeChannel(channel);
            }
        }));
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 22:46
     * @description: 7节新增，向所有连接成功的提供者发送ping消息
     */
    public static void sendPingFromConsumer(){
        Set<Channel> channels = ConsumerChannelCache.getChannelCache();
        if (channels == null || channels.isEmpty()) return;

        //构建请求协议对象，封装ping消息
        RpcProtocol<RpcRequest> requestProtocol = new RpcProtocol<>();
        //消息头
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
        //消息体
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        //封装
        requestProtocol.setHeader(header);
        requestProtocol.setBody(request);

        //每个channel发送一次ping消息
        channels.forEach((channel -> {
            if (channel.isOpen() && channel.isActive()){
                log.info("消费者{}向提供者{}发送ping消息，消息内容为：{}",channel.localAddress(),channel.remoteAddress(),RpcConstants.HEARTBEAT_PING);
            }
            //发送
            channel.writeAndFlush(requestProtocol);
        }));
    }
}
