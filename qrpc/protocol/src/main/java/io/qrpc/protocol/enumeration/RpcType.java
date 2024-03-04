package io.qrpc.protocol.enumeration;

/**
 * @ClassName: RpcType
 * @Author: qiuzhiq
 * @Date: 2024/1/17 16:37
 * @Description: 协议类型，分三种
 */

public enum RpcType {

    //请求消息
    REQUEST(1),
    //响应消息
    RESPONSE(2),

    //心跳消息
    HEARTBEAT_FROM_CONSUMER(3),//消费者发送的ping消息
    HEARTBEAT_FROM_PROVIDER(5),//提供者发送的ping消息
    HEARTBEAT_TO_PROVIDER(6), //消费者发送的pong消息
    HEARTBEAT_TO_CONSUMER(4); //提供者发送的pong消息

    private final int type;

    RpcType(int type) {
        this.type = type;
    }

    public static RpcType findByType(int type) {
        for (RpcType rpcType : RpcType.values()) {
            if (rpcType.getType() == type) return rpcType;
        }
        return null;
    }

    public int getType() {
        return type;
    }


}
