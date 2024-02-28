package io.qrpc.test.spi.service;

import io.qrpc.spi.annotation.SPI;

/**
 * @InterfaceName: SPIService
 * @Author: qiuzhiq
 * @Date: 2024/2/28 0:52
 * @Description:
 */
@SPI("spiService")
public interface SPIService {
    String hello(String name);
}
