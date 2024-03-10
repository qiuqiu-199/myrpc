package io.qrpc.demo.springboot.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName: ProviderStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/9 16:57
 * @Description:
 */
@SpringBootApplication
@ComponentScan(value = "io.qrpc.demo")
public class ProviderStarter {
    public static void main(String[] args) {
        SpringApplication.run(ProviderStarter.class,args);
    }
}
