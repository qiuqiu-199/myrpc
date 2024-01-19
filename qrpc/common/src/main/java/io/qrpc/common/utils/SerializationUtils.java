package io.qrpc.common.utils;

import java.util.stream.IntStream;

/**
 * @ClassName: SerializationUtils
 * @Author: qiuzhiq
 * @Date: 2024/1/18 9:44
 * @Description: 针对消息头的序列化类型的操作
 */

public class SerializationUtils {
    private static final String PADDING_STRING = "0";

    public static final int MAX_SERIALIZATION_TYPE_COUNT = 16;

    /**f
     * 序列化类型最大长度为16，不足的补齐
     * @param str 原始字符串
     * @return 补齐0后的字符串
     */
    public static String PaddingString(String str) {
        str = str == null ? "" : str;  //字符串为null的话就转为空字符串
        if (str.length() >= MAX_SERIALIZATION_TYPE_COUNT) return str;  //TODO 最长16，为么超过16又直接返回而不是截掉16之后的呢？

        int padingcount = MAX_SERIALIZATION_TYPE_COUNT -str.length();
        StringBuilder sb = new StringBuilder(str);
        //for循环的另一种形式
        IntStream.range(0,padingcount).forEach((i)->{
            sb.append(PADDING_STRING);
        });
//        for (int i = 0; i < padingcount; i++) {
//            sb.append(PADDING_STRING);
//        }

        return sb.toString();
    }


    /**f
     * 去0操作
     * @param str 原始字符串
     * @return 去掉0后的字符串
     */
    public static String subString(String str){
        return str == null ? "" : str.replace(PADDING_STRING,"");
    }
}
