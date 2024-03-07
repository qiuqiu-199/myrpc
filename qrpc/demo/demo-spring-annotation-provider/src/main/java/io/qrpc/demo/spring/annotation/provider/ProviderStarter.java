package io.qrpc.demo.spring.annotation.provider;

import io.qrpc.demo.spring.annotation.provider.config.ProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @ClassName: ProviderStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:51
 * @Description:
 */

public class ProviderStarter {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(ProviderConfig.class);
    }
}
