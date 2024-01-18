package io.qrpc.protocol.header;

import io.qrpc.common.id.IdFactory;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.enumeration.RpcType;

/**
 * @ClassName: RpcHeaderFactory
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:06
 * @Description: 生成消息头的工厂
 */

public class RpcHeaderFactory  {
    public static RpcHeader getRequestHeader(String serializationType){
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setMagic(RpcConstants.MAGIC);
        rpcHeader.setMsgType((byte) RpcType.REQUEST.getType());
        rpcHeader.setStatus((byte)0x1);
        rpcHeader.setRequestId(IdFactory.getId());
        rpcHeader.setSerailizationType(serializationType);
//        rpcHeader.setMsgLen();
        return rpcHeader;
    }
}
