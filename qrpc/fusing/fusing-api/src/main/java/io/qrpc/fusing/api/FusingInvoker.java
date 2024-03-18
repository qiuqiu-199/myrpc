package io.qrpc.fusing.api;

import io.qrpc.spi.annotation.SPI;

/**
 * @InterfaceName: FusingInvoker
 * @Author: qiuzhiq
 * @Date: 2024/3/16 16:07
 * @Description:
 */
@SPI
public interface FusingInvoker {

    /**
     * @author: qiu
     * @date: 2024/3/16 16:10
     * @description: 核心方法，由扩展实现如何进行服务熔断
     */
    boolean invokeFusingStrategy();

    //以下两个方法，一个调用服务成功调用，一个失败调用，增加成功调用和失败调用的次数
    // 次数用于衡量什么时候断路器进入开启、半开启状态
    // 由基础类实现，扩展类统一使用
    void incrementCount();
    void incrementFailureCount();

    /**
     * @author: qiu
     * @date: 2024/3/16 16:15
     * @description: 由基础类实现，扩展类统一使用。初始化断路器，需要周期和失败次数上限。
     */
    void init(int totalFailure, int milliSeconds);
}
