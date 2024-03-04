package io.qrpc.provider.common.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.common.threadPool.ServerThreadPool;
import io.qrpc.constants.RpcConstants;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcStatus;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import io.qrpc.provider.common.cache.ProviderConnectionCache;
import io.qrpc.reflect.api.ReflectInvoker;
import io.qrpc.spi.loader.ExtensionLoader;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @ClassName: RpcProviderHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:33
 * @Description: 对来自服务消费者的数据处理器。到这一步，前面已经做完解码工作了。
 */

public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    private final Map<String, Object> handlerMap;

    //反射扩展接口，创建当前类对象时加载对应的反射方式
    private final ReflectInvoker reflectInvoker;

    /**
     * @author: qiu
     * @date: 2024/2/29 22:44
     * @param: handlerMap
     * @param: reflectType
     * @return: null
     * @description: 37章修改。构造方法由传递过来的反射类型加载对应的反射方式
     */
    public RpcProviderHandler(Map<String, Object> handlerMap, String reflectType) {
        this.handlerMap = handlerMap;
        reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 11:03
     * @description: 7节修改，连接成功后将channel缓存到连接缓存里
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderConnectionCache.addChannel(ctx.channel());
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 11:04
     * @description: 7节新增，连接断开后，缓存中移除连接
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderConnectionCache.removeChannel(ctx.channel());
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 11:04
     * @description: 7节新增，连接断开后，缓存中移除连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderConnectionCache.removeChannel(ctx.channel());
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 11:05
     * @description: 7节新增，服务端触发超时事件后断开连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            Channel channel = ctx.channel();
            try {
                LOGGER.info("在提供者的netty服务端触发超时事件，准备关闭channel：{}",channel);
                channel.close();
            }finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    //第8章模拟接收消费者的数据的临时处理，接收到数据后构造处理完毕后的数据回送给消费者
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ServerThreadPool.submit(() -> {
            //处理消息并得到处理结果，包装到协议对象中
            RpcProtocol<RpcResponse> responseProtocol = handleMessage(protocol,ctx.channel());

            //发送响应报文
            //增加监听器，如果报文发送成功就log一下
            ctx.writeAndFlush(responseProtocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    LOGGER.debug("已发送对id为 {} 的响应。", responseProtocol.getHeader().getRequestId());
                }
            });
        });
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 20:34
     * @description: 7节新增，根据消息类型调用不同处理方法并返回处理结果。
     */
    private RpcProtocol<RpcResponse> handleMessage(RpcProtocol<RpcRequest> protocol,Channel channel) {
        RpcProtocol<RpcResponse> responseProtocol = null;

        byte msgType = protocol.getHeader().getMsgType();

        if (msgType == RpcType.REQUEST.getType()) {
            responseProtocol = handleRequest(protocol);
        } else if (msgType == RpcType.HEARTBEAT_FROM_CONSUMER.getType()) {
            responseProtocol = handleHeartBeatFromConsumer(protocol,channel);
        } else if (msgType == RpcType.HEARTBEAT_TO_PROVIDER.getType()){
            handleHeartBeatToProvider(protocol,channel);
        }

        return responseProtocol;
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 20:33
     * @description: 7节新增。处理请求消息并返回响应消息
     */
    private RpcProtocol<RpcResponse> handleRequest(RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();

        //设置消息类型为响应类型
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        LOGGER.debug("接收到请求的请求id：{}", header.getRequestId());

        //调用真实方法，根据结果构建响应体
        RpcResponse responseBody = new RpcResponse();
        RpcRequest requestBody = protocol.getBody();
        try {
            Object result = handle(requestBody);
            responseBody.setResult(result);
            responseBody.setOneway(requestBody.isOneway());
            responseBody.setAsync(requestBody.isAsync());
            header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        } catch (Throwable t) {
            responseBody.setError(t.toString());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            LOGGER.error("服务调用过程出错:" + t);
        }

        responseProtocol.setHeader(header);
        responseProtocol.setBody(responseBody);

        return responseProtocol;
    }

    /**
     * @author: qiu
     * @date: 2024/2/29 22:45
     * @description: 解析请求体，预备通过反射调用真实方法，返回真实方法处理结果
     * 37章修改，引入SPI机制动态加载反射方式
     */
    private Object handle(RpcRequest requestBody) throws Throwable {
        //根据请求体要调用的方法的信息获取对应的Class对象
        String serviceKey = RpcServiceHelper.buildServiceKey(requestBody.getClassName(), requestBody.getVersion(), requestBody.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("服务不存在：%s:%s", requestBody.getClassName(), requestBody.getMethodName()));
        }

        //准备通过反射调用真实方法
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = requestBody.getMethodName();
        Class<?>[] parameterTypes = requestBody.getParameterTypes();
        Object[] parameters = requestBody.getParameters();

        LOGGER.debug("==========调用信息如下：");
        LOGGER.debug("调用服务：{}:{}", serviceClass.getName(), methodName);
        LOGGER.debug("调用参数类型：");
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (Class<?> parameterType : parameterTypes) {
                LOGGER.debug(parameterType.getName());
            }
        } else {
            LOGGER.debug("无参数");
        }
        LOGGER.debug("调用参数值：");
        if (parameters != null && parameters.length > 0) {
            for (Object parameter : parameters) {
                LOGGER.debug(parameter.toString());
            }
        } else {
            //TODO 这里需要想想异常情况，比如参数类型数组长度和参数值数组长度对应不上
            LOGGER.debug("无参数值");
        }

        return this.reflectInvoker.invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 20:36
     * @description: 7节新增，处理心跳消息并返回pong消息
     */
    private RpcProtocol<RpcResponse> handleHeartBeatFromConsumer(RpcProtocol<RpcRequest> protocol, Channel channel) {
        LOGGER.info("接收到来自消费者{}发送的ping消息，消息内容为{}",channel.remoteAddress(),protocol.getBody().getParameters()[0]);
        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();

        //消息头设置
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_CONSUMER.getType());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        //消息体设置
        RpcResponse responseBody = new RpcResponse();
        responseBody.setResult(RpcConstants.HEARTBEAT_PONG);
        responseBody.setAsync(protocol.getBody().isAsync());
        responseBody.setOneway(protocol.getBody().isOneway());

        responseProtocol.setHeader(header);
        responseProtocol.setBody(responseBody);

        return responseProtocol;
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 21:00
     * @description: 7节新增，收到消费者响应的pong消息就log
     */
    private void handleHeartBeatToProvider(RpcProtocol<RpcRequest> protocol, Channel channel) {
        LOGGER.info("接收到来自消费者{}的pong消息，消息内容为：{}",channel.remoteAddress(),protocol.getBody().getParameters()[0]);
    }

    /**
     * @author: qiu
     * @date: 2024/3/3 23:54
     * @description: 7节新增
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ProviderConnectionCache.removeChannel(ctx.channel());
        ctx.channel().close();
    }
}
