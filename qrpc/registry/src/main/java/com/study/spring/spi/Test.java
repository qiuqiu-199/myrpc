package com.study.spring.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @ClassName: Test
 * @Author: qiuzhiq
 * @Date: 2024/2/26 22:16
 * @Description:
 */

public class Test {
    public static void main(String[] args) {
        ServiceLoader<Phone> phones = ServiceLoader.load(Phone.class);
        Iterator<Phone> it = phones.iterator();
        while (it.hasNext()){
            System.out.println(it.next());
//            it.next().getName();
        }
    }
}
