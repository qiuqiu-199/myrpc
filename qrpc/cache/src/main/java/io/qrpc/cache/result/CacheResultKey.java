package io.qrpc.cache.result;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @ClassName: CacheResultKey
 * @Author: qiuzhiq
 * @Date: 2024/3/12 9:44
 * @Description: 请求的key，带时间戳，通过6要素判断请求是否相同
 */

public class CacheResultKey implements Serializable {
    private static final long serialVersionUID = -4621054360698822885L;

    //时间戳
    @Setter
    @Getter
    private long cacheTimeStamp;

    //6要素
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private String version;
    private String group;

    public CacheResultKey(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters, String version, String group) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        this.version = version;
        this.group = group;
    }

    /**
     * 重写equals需要满足的条件：
     * - 自反性：对于任何非空引用值 x，x.equals(x) 都应返回 true
     * - 排他性：当比对的不是同种类型的对象或者是一个null时，默认返回false
     * - 对称性：对于任何非空引用值 x 和 y，当且仅当 y.equals(x) 返回 true 时，x.equals(y) 才应返回 true
     * - 传递性：对于任何非空引用值 x、y 和 z，如果 x.equals(y) 返回 true，并且 y.equals(z) 返回 true，那么x.equals(z) 应返回 true
     * - 一致性：只要非空引用值x和y的状态没有改变，多次调用 x.equals(y) 始终返回 true 或始终返回 false
     * <p>
     * 下面这个写法是一个比较标准的写法，以后的equals方法重写按照这个格式来写
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; //1.自反性
        //2.排他性，使用getClass方法判断是否为同类型对象
        if (obj == null || getClass() != obj.getClass()) return false;
        //上面这个方式比较严格，下面这个方式也可以，相对没那么严格
        //if (!(obj instanceof CacheResultKey)) return false;

        //3、4、5：只需要重写各成员变量是否相同即可满足
        CacheResultKey cacheResultKey = (CacheResultKey) obj;
        return Objects.equals(className, cacheResultKey.className)
                && Objects.equals(methodName, cacheResultKey.methodName)
                && Arrays.equals(parameterTypes, cacheResultKey.parameterTypes)
                && Arrays.equals(parameters, cacheResultKey.parameters)
                && Objects.equals(version, cacheResultKey.version)
                && Objects.equals(group, cacheResultKey.group);
    }

    //使用31的原因见笔记
    @Override
    public int hashCode() {
        int res = Objects.hash(className, methodName, version, group);
        res = 31 * res + Arrays.hashCode(parameterTypes);
        res = 31 * res + Arrays.hashCode(parameters);
        return res;
    }
}
