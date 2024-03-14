package io.qrpc.connection.disuse.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: Test
 * @Author: qiuzhiq
 * @Date: 2024/3/13 20:25
 * @Description:
 */

public class Test {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("3",366666);
        map.put("2",277);
        map.put("88",955561);
        map.put("4",7777);
        for (int i = 0; i < 4; i++) {
            System.out.print(i + "===");
            ArrayList<Integer> list = new ArrayList<>(map.values());
            list.forEach(obj -> System.out.print(obj + ","));
            System.out.println();
        }
    }
}
