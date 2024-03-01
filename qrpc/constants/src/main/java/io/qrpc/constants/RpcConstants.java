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
}
