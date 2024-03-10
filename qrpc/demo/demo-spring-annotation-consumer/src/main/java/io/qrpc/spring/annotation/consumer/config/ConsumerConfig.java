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
//@ComponentScan(value = "io.qrpc.demo")不可行，因为RpcConsumerPostProcessor也要注入容器，否则DemoService注入失败
public class ConsumerConfig {
}
