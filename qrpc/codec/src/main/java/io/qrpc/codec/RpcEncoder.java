package io.qrpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.qrpc.common.utils.SerializationUtils;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.serialization.api.Serialization;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName: RpcEncoder
 * @Author: qiuzhiq
 * @Date: 2024/1/18 10:39
 * @Description: 基于netty实现数据的编码
 */

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec{

    /**
     * @author: qiu
     * @date: 2024/2/28 22:14
     * @param: ctx
     * @param: msg
     * @param: byteBuf
     * @return: void
     * @description:
     * 26章修改，通过SPI来加载需要的序列化类型
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        //编码，先写入消息头
        RpcHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());


        //序列化类型
        String serailizationType = header.getSerailizationType();
        byteBuf.writeBytes(SerializationUtils.PaddingString(serailizationType).getBytes(StandardCharsets.UTF_8));
        //将消息体序列化并写入
        Serialization serialization = getJdkSerialization(serailizationType);
        byte[] bytes = serialization.serialize(msg.getBody());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
