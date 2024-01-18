import io.qrpc.common.exception.SerializerException;
import io.qrpc.common.utils.SerializationUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @ClassName: SomeTest
 * @Author: qiuzhiq
 * @Date: 2024/1/18 9:54
 * @Description:
 */

public class SomeTest {
    @Test
    public void test1(){
        int count = 0;
        int sum = 0;
        while(count++ < 10){
            long s = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            IntStream.range(0,100000000).forEach((i)->{
                sb.append("0");
            });
//            for (int i = 0; i < 100; ++i){
//                sb.append("0");
//            }
            long end = System.currentTimeMillis();
            sum+=(end-s);
            System.out.println("第"+count+"次结果====》"+(end-s));
        }

        System.out.println("平均每次花费时间："+(sum/count));

        System.out.println("++++++++++++for循环");
        count = 0;
        sum = 0;
        while(count++ < 10){
            long s = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
//            IntStream.range(0,100).forEach((i)->{
//                sb.append("0");
//            });
            for (int i = 0; i < 100000000; ++i){
                sb.append("0");
            }
            long end = System.currentTimeMillis();
            sum+=(end-s);
            System.out.println("第"+count+"次结果====》"+(end-s));
        }

        System.out.println("平均每次花费时间："+(sum/count));
        System.out.println("平均每次花费时间："+(sum/count));

    }
}
