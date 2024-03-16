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

    //笔记11章，结果缓存相关key
    public static final int CACHERESULT_SCAN_INTERNAL = 500000; //定期清理过期缓存间隔时间
    public static final int CACHERESULT_SCAN_EXPIRE = 5000; //缓存时间

    //连接管理-连接淘汰策略
    public static final String CONNECTION_DISUSE_STRATEGY_DEFAULT = "strategy_default";
    public static final String CONNECTION_DISUSE_STRATEGY_RANDOM = "random";
    public static final String CONNECTION_DISUSE_STRATEGY_FIRST = "first";
    public static final String CONNECTION_DISUSE_STRATEGY_LFU = "lfu";
    public static final String CONNECTION_DISUSE_STRATEGY_LRU = "lru";
    public static final String CONNECTION_DISUSE_STRATEGY_REFUSE = "refuse";

    //容错层相关
    //服务降级-默认的容错处理类
    public static final Class<?> FALLBACK_CLASS_DEFAULT = void.class;
    //服务限流-默认限流策略：计数器
    public static final String RATE_LIMITER_DEFAULT = "counter";
    public static final String RATE_LIMITER_FAIL_STRATEGY_EXCEPTION = "exception";
    public static final String RATE_LIMITER_FAIL_STRATEGY_FALLBACK = "fallback";
    public static final String RATE_LIMITER_FAIL_STRATEGY_DIRECT = "direct";
}
