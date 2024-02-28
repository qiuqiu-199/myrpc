package com.study.spring.spi;

/**
 * @ClassName: Pingguo
 * @Author: qiuzhiq
 * @Date: 2024/2/26 22:23
 * @Description:
 */

public class Pingguo implements Phone {
    @Override
    public void getName() {
        System.out.println("pingguo");
    }
}
