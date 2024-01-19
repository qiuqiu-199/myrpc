package io.qrpc.protocol.request;

import io.qrpc.protocol.base.RpcMessage;

/**
 * @ClassName: RpcRequest
 * @Author: qiuzhiq
 * @Date: 2024/1/17 16:49
 * @Description: Rpc请求消息的封装，对应的请求id再消息头中
 */

public class RpcRequest extends RpcMessage {
    private static final long serialVersionUID = 5645393643458897838L;

    //类名
    private String className;
    //方法名
    private String methodName;
    //参数类型数组
    private Class<?>[] parameterTypes;
    //参数值数组
    private Object[] parameters;
    //版本
    private String version;
    //分组
    private String group;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
