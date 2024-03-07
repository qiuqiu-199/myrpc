package io.qrpc.spring.annotation.consumer.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ConsumerConfig
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:28
 * @Description: 配置类，没有需要注入的组件，所以这里仅用于指定扫描路径
 */

@Configuration
@ComponentScan(value = "io.qrpc.*")
public class ConsumerConfig {
}
