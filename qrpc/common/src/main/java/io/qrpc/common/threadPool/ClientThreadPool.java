package io.qrpc.common.threadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ClientThreadPool
 * @Author: qiuzhiq
 * @Date: 2024/2/17 10:53
 * @Description: 3.5节新增服务消费者线程池类
 */

public class ClientThreadPool {
    private static ThreadPoolExecutor pool;

    static {
        pool = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));
    }


    public static void submit(Runnable task){ pool.submit(task);}

    public static void shutdowm(){pool.shutdown();}
}
