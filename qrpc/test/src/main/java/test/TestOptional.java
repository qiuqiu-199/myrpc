package test;

import java.util.Optional;

/**
 * @ClassName: TestOptional
 * @Author: qiuzhiq
 * @Date: 2024/2/28 14:15
 * @Description:
 */

public class TestOptional {
    public static void main(String[] args) {
        String name = "沉默王二";
        Optional<String> nameOptional = Optional.of(name);
        Optional<Integer> intOpt = nameOptional
                .map(String::length);

        System.out.println( intOpt.orElse(0));
    }
}
