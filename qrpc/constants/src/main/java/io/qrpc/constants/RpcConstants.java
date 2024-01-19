package io.qrpc.constants;

/**
 * @ClassName: RpcConstants
 * @Author: qiuzhiq
 * @Date: 2024/1/17 17:10
 * @Description:
 */

public class RpcConstants {
    public static final int HEADER_TOTAL_LEN = 32;
    public static final short MAGIC=0x10;


    //反射类型
    public static final String REFLECT_TYPE_JDK = "jdk";
    public static final String REFLECT_TYPE_CGLIB = "cglib";
}
