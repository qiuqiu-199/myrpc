package io.qrpc.test.spi.service.impl;

import io.qrpc.spi.annotation.SpiClass;
import io.qrpc.test.spi.service.SPIService;

/**
 * @ClassName: SPIServiceImpl
 * @Author: qiuzhiq
 * @Date: 2024/2/28 0:51
 * @Description:
 */
@SpiClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "impl, hello " + name;
    }
}
