package io.qrpc.spring.annotation.consumer;

import io.qrpc.spring.annotation.consumer.config.ConsumerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @ClassName: ConsumerStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:29
 * @Description:
 */

public class ConsumerStarter {
    private static Logger logger = LoggerFactory.getLogger(ConsumerStarter.class);

    @Test
    public void startClient(){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConsumerConfig.class);

        ConsumerDemoService consumerDemoService = ctx.getBean(ConsumerDemoService.class);
        String res = consumerDemoService.otherBusiness("client with spring-annotation");

        logger.info("以注解形式接入spring的结果：{}",res);
    }
}
