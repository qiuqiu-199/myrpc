package com.study.spring.spi;

/**
 * @ClassName: Huawei
 * @Author: qiuzhiq
 * @Date: 2024/2/26 22:22
 * @Description:
 */

public class Huawei implements Phone {
    @Override
    public void getName() {
        System.out.println("huawei");
    }
}
