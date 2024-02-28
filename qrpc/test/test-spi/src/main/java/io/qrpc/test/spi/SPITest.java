package io.qrpc.test.spi;

import io.qrpc.spi.loader.ExtensionLoader;
import io.qrpc.test.spi.service.SPIService;

/**
 * @ClassName: SPITest
 * @Author: qiuzhiq
 * @Date: 2024/2/28 0:53
 * @Description:
 */

public class SPITest {
    public static void main(String[] args) {
        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        System.out.println(spiService.hello("uuuuuuu"));
    }
}
