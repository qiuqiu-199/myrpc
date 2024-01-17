package io.qrpc.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: RpcProviderHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:33
 * @Description: 对来自服务消费者的数据处理器
 */

public class RpcProviderHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    private final Map<String,Object> handlerMap;
    public RpcProviderHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        LOGGER.info("RPC服务提供者收到的数据为====>>>" + o.toString());
        LOGGER.info("handlerMap中村烦的数据如下：");
        handlerMap.forEach((key, value) -> {
            LOGGER.info(key + "====" + value);
        });

        channelHandlerContext.writeAndFlush(o);
    }
}
