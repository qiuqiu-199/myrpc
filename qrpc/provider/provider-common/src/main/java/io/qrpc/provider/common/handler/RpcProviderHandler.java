package io.qrpc.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.request.RpcRequst;
import io.qrpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: RpcProviderHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:33
 * @Description: 对来自服务消费者的数据处理器。到这一步，前面已经做完解码工作了
 */

public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequst>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    private final Map<String,Object> handlerMap;

    public RpcProviderHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    //第8章模拟接收消费者的数据的临时处理，接收到数据后构造处理完毕后的数据回送给消费者
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequst> protocol) throws Exception {
        LOGGER.info("========================接收数据如下：");
        LOGGER.info("RPC服务提供者收到的数据为====>>>" + protocol.toString());
        LOGGER.info("handlerMap中存放的数据如下：");
        handlerMap.forEach((key, value) -> {
            LOGGER.info(key + "====" + value);
        });

        RpcHeader header = protocol.getHeader();
        RpcRequst requst = protocol.getBody();

        //构建响应消息
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();
        responseProtocol.setHeader(header);

        RpcResponse response = new RpcResponse();
        response.setResult("接收数据成功！。。。处理数据成功！");
        response.setAsync(requst.isAsync());
        response.setOneway(requst.isOneway());
        responseProtocol.setBody(response);

        //写入
        ctx.writeAndFlush(responseProtocol);
    }
}
