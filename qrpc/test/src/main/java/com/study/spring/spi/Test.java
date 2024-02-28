package com.study.spring.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ServiceLoader<Phone> phones = ServiceLoader.load(Phone.class);
        Iterator<Phone> it = phones.iterator();
        while (it.hasNext()){
//            System.out.println(it.next());
            it.next().getName();
        }
//
//        Phone o = (Phone) Class.forName("com.study.spring.spi.Huawei").newInstance();
//        o.getName();
    }
}
