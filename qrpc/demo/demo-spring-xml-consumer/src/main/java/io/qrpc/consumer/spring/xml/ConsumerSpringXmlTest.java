package io.qrpc.consumer.spring.xml;

import io.qrpc.consumer.RpcClient;
import io.qrpc.demo.api.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @ClassName: ConsumerSpringXmlTest
 * @Author: qiuzhiq
 * @Date: 2024/3/6 15:40
 * @Description: 测试xml形式的spring整合
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class ConsumerSpringXmlTest {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerSpringXmlTest.class);

//  组件注入
    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testClientWith_SpringXml() throws IOException {
        DemoService demoService = rpcClient.create(DemoService.class);
        String res = demoService.sayHello("cleint with spring-xml");

        logger.info("以xml方式接入消费者的结果：{}",res);

        System.in.read();
    }
}
