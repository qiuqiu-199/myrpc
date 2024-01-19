package io.qrpc.common.threadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ServerThreadPool
 * @Author: qiuzhiq
 * @Date: 2024/1/19 12:15
 * @Description: 线程池工具类，在handler里给提供者执行异步任务用
 */

public class ServerThreadPool {
    private static ThreadPoolExecutor pool;

    static {
        pool = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
    }

    public static void submit(Runnable task) {
        pool.submit(task);
    }

    public static void shutdown() {
        pool.shutdown();
    }
}
