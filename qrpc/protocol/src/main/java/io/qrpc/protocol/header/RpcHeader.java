package io.qrpc.protocol.header;

import java.io.Serializable;

/**
 * @ClassName: RpcHeader
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:01
 * @Description: 消息头，目前固定为32个字节
 */

public class RpcHeader implements Serializable {

    private static final long serialVersionUID = -4488024662007750673L;

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 报文类型 1byte | 状态 1byte |     消息 ID 8byte      |
    +---------------------------------------------------------------+
    |           序列化类型 16byte      |        数据长度 4byte          |
    +---------------------------------------------------------------+
    */

    private short magic;
    private byte msgType;
    private byte status;
    private long requestId;
    //16字节的序列化类型，不足16为后面补0，约定该字段长度最多16
    private String serailizationType;
    private int msgLen;

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getSerailizationType() {
        return serailizationType;
    }

    public void setSerailizationType(String serailizationType) {
        this.serailizationType = serailizationType;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public void setMsgLen(int msgLen) {
        this.msgLen = msgLen;
    }
}
