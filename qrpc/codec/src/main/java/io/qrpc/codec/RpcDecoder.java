package io.qrpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.qrpc.common.exception.SerializerException;
import io.qrpc.common.utils.SerializationUtils;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import io.qrpc.serialization.api.Serialization;

import java.util.List;

/**
 * @ClassName: RpcDecoder
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:51
 * @Description: 消息的解码
 */

public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {

    /**
     * @author: qiu
     * @date: 2024/2/28 22:11
     * @param: ctx
     * @param: byteBuf
     * @param: list
     * @return: void
     * @description: 消息解码
     * 26章修改，根据请求头的序列化类型参数选择对应的序列化实现类对消息进行反序列化
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) return;

        byteBuf.markReaderIndex();  //标记一下

        //检查魔数
        short magic = byteBuf.readShort();
        if (magic != RpcConstants.MAGIC) throw new SerializerException("magic number is illegal" + magic);

        //获取报文类型
        byte msgType = byteBuf.readByte();
        RpcType msgTypeEnum = RpcType.findByType(msgType);
        if (msgTypeEnum == null) return;
        //报文状态
        byte msgStatus = byteBuf.readByte();
        //请求消息ID
        long requestId = byteBuf.readLong();

        //获取序列化类型
        ByteBuf serializationTypeBuf = byteBuf.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_COUNT);
        String serializationType = SerializationUtils.subString(serializationTypeBuf.toString(CharsetUtil.UTF_8));

        //检查数据长度并获取数据
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);


        //还原消息头
        RpcHeader header = new RpcHeader();
        header.setMagic(magic);//魔数
        header.setMsgType(msgType);//消息类型
        header.setStatus(msgStatus);//消息状态
        header.setRequestId(requestId);//消息ID
        header.setSerailizationType(serializationType);//序列化类型
        header.setMsgLen(dataLength);//消息长度
        //序列化类型选择，待扩展
        //26章已扩展
        Serialization jdkSerialization = getSerialization(serializationType);

        //根据消息类型还原成不同的消息协议
        switch (msgTypeEnum) {
            //来自消费者的消息统一反序列化为RpcRequest，不管是请求消息、消费者发送的ping消息还是pong消息
            case REQUEST:
            case HEARTBEAT_FROM_CONSUMER:
            case HEARTBEAT_TO_PROVIDER:
                RpcRequest rpcRequest = jdkSerialization.desrialize(data, RpcRequest.class);
                if (rpcRequest != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(rpcRequest);

                    list.add(protocol);
                }
                break;

            //来自提供者的消息统一反序列化为RpcResponse，不管是响应消息、提供者发送的ping消息还是pong消息
            case RESPONSE:
            case HEARTBEAT_FROM_PROVIDER:
            case HEARTBEAT_TO_CONSUMER:
                RpcResponse rpcResponse = jdkSerialization.desrialize(data, RpcResponse.class);
                if (rpcResponse != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(rpcResponse);

                    list.add(protocol);
                }
        }
    }
}
