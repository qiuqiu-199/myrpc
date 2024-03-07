package io.qrpc.demo.provier.spring.xml;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @ClassName: io.qrpc.demo.provier.spring.xml.ProviederSpringXmlStarter
 * @Author: qiuzhiq
 * @Date: 2024/3/6 11:05
 * @Description:
 */

public class ProviederSpringXmlStarter {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
