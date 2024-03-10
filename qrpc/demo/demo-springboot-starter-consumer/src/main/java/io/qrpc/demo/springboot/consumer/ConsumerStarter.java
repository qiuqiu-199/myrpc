package io.qrpc.demo.springboot.consumer;

import io.qrpc.demo.springboot.consumer.service.ConsumerDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName: ConsuemrStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/9 20:06
 * @Description:
 */
@SpringBootApplication
@ComponentScan(value = "io.qrpc.*")
//@ComponentScan(value = "io.qrpc.demo")不可行，因为RpcConsumerPostProcessor也要注入容器，否则DemoService注入失败
public class ConsumerStarter {
    private final static Logger log = LoggerFactory.getLogger(ConsumerStarter.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ConsumerStarter.class, args);

        ConsumerDemoService consumerDemoService = ctx.getBean(ConsumerDemoService.class);
        String res = consumerDemoService.otherBusiness("qiuzhiq");
        log.info("整合spring-boot的结果...=====》"+ res);
    }
}
