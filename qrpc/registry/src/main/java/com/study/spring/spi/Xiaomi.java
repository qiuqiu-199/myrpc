package com.study.spring.spi;

/**
 * @ClassName: Xiaomi
 * @Author: qiuzhiq
 * @Date: 2024/2/26 22:22
 * @Description:
 */

public class Xiaomi implements Phone {
    @Override
    public void getName() {
        System.out.println("xiaomi");
    }
}
