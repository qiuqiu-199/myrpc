package io.qrpc.provider.common.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.qrpc.common.helper.RpcServiceHelper;
import io.qrpc.common.threadPool.ServerThreadPool;
import io.qrpc.protocol.RpcProtocol;
import io.qrpc.protocol.enumeration.RpcStatus;
import io.qrpc.protocol.enumeration.RpcType;
import io.qrpc.protocol.header.RpcHeader;
import io.qrpc.protocol.request.RpcRequest;
import io.qrpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @ClassName: RpcProviderHandler
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:33
 * @Description: 对来自服务消费者的数据处理器。到这一步，前面已经做完解码工作了
 */

public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    //第8章模拟接收消费者的数据的临时处理，接收到数据后构造处理完毕后的数据回送给消费者
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        ServerThreadPool.submit(() -> {
            RpcHeader header = protocol.getHeader();
            RpcRequest requestBody = protocol.getBody();

            header.setMsgType((byte) RpcType.RESPONSE.getType());
            LOGGER.debug("接收到请求的请求id：{}", header.getRequestId());

            //调用真实方法,根据结果构建响应体
            RpcResponse responseBody = new RpcResponse();
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

            //根据响应头和响应体构建响应报文
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            resProtocol.setHeader(header);
            resProtocol.setBody(responseBody);

            //发送响应报文
            //增加监听器，如果报文发送成功就log一下
            ctx.writeAndFlush(resProtocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    LOGGER.debug("已发送对id为 {} 的调用结果响应。", header.getRequestId());
                }
            });
        });
    }

    /**f
     * 调用方法
     * @param requestBody 响应体
     * @return 响应结果
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object handle(RpcRequest requestBody) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String serviceKey = RpcServiceHelper.buildServiceKey(requestBody.getClassName(), requestBody.getVersion(), requestBody.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("服务不存在：%s:%s", requestBody.getClassName(), requestBody.getMethodName()));
        }

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

        return invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);

    }

    /**f
     * 通过反射调用方法
     * @param serviceBean
     * @param serviceClass
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);

        return method.invoke(serviceBean, parameters);
    }
}
