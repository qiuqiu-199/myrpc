package io.qrpc.constants;

/**
 * @ClassName: RpcConstants
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:10
 * @Description:
 */

public class RpcConstants {
    //消息头，固定32字节
    public static final int HEADER_TOTAL_LEN = 32;
    //魔数
    public static final short MAGIC=0x10;


    //反射类型
    public static final String REFLECT_TYPE_JDK = "jdk";
    public static final String REFLECT_TYPE_CGLIB = "cglib";

    //序列化类型
    public static final String SERIALIZATION_JDK = "jdk";
    public static final String SERIALIZATION_JSON = "json";
    public static final String SERIALIZATION_PROTOSTUFF = "protostuff";
    public static final String SERIALIZATION_HESSIAN2 = "hessian2";

    //负载均衡
    public static final String SERVICE_LOAD_BALANCER_RANDOM = "random";
    public static final String SERVICE_LOAD_BALANCER_ZKCONSISTENTHASH = "zkconsistenthash";

    //加权版负载均衡
    public static final String SERVICE_ENHANCED_LOAD_BALANCE_PREFIX = "enhanced_";//加权版权重前缀
    public static final int SERVICE_WEIGHT_MIN = 1;//最小权重
    public static final int SERVICE_WEIGHT_MAX = 100;//最高权重

    //注册中心类型及对应的注册中心地址
    public static final String REGISTRY_TYPE_ZOOKEEPER = "zookeeper";
    public static final String REGISTRY_TYPE_ZOOKEEPER_ADDR = "127.0.0.1:2181";
    public static final String REGISTRY_TYPE_NACOS = "nacos";
    public static final String REGISTRY_TYPE_NACOS_ADDR = "127.0.0.1:8848";

    //7节，新增心跳消息类型
    public static final String HEARTBEAT_PING = "ping";
    public static final String HEARTBEAT_PONG = "pong";

    //7节，新增定义一些设置channel处理器的key
    public static final String CODEC_ENCODER = "encoder";
    public static final String CODEC_DEVODER = "decoder";
    public static final String CODEC_HANDLER = "handle";
    public static final String CODEC_SERVER_IDEL_HANDLER = "server-idle-handler";
    public static final String CODEC_CLIENT_IDLE_HANDLER = "client-idle-handler";
}
