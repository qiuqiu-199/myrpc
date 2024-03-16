package io.qrpc.cache.result;

import io.qrpc.constants.RpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName: CacheResultManager
 * @Author: qiuzhiq
 * @Date: 2024/3/12 10:58
 * @Description: 结果缓存管理器，单例对象，可以定期清理过期缓存
 */

public class CacheResultManager<T> {
    private static final Logger log = LoggerFactory.getLogger(CacheResultManager.class);
    //缓存容器
    private final Map<CacheResultKey, T> cacheResultMap = new ConcurrentHashMap<>();
    //定时任务线程池，用于定时删除过期缓存
    private ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    //锁
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    //过期时间，创建缓存时指定
    private int cacheResultExpire;

    /**
     * @author: qiu
     * @date: 2024/3/12 11:19
     * @description: 构造方法，可以配置是否开启缓存，如果开启，会自动定时清理缓存
     */
    private static volatile CacheResultManager instance;

    private CacheResultManager(boolean enableResultCache, int cacheResultExpire) {
        this.cacheResultExpire = cacheResultExpire;
        if (enableResultCache) {
            this.scheduledRemoveCacheTask();
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/12 11:23
     * @description: 缓存为单例对象
     */
    public static <T> CacheResultManager<T> getInstance(boolean enableResultCache, int cacheResultExpire) {
        if (instance == null) {
            synchronized (CacheResultManager.class) {
                if (instance == null) {
                    instance = new CacheResultManager(enableResultCache, cacheResultExpire);
                }
            }
        }
        return instance;
    }

    /**
     * @author: qiu
     * @date: 2024/3/12 11:18
     * @description: 定时删除缓存中过期的响应
     */
    private void scheduledRemoveCacheTask() {
        log.warn("清除缓存中的过期响应中");
        pool.scheduleAtFixedRate(() -> {
            if (cacheResultMap.size() > 0) {
                writeLock.lock();
                try {
                    Iterator<Map.Entry<CacheResultKey, T>> iterator = cacheResultMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<CacheResultKey, T> entry = iterator.next();
                        CacheResultKey key = entry.getKey();
                        if (System.currentTimeMillis() - key.getCacheTimeStamp() > cacheResultExpire){
                            cacheResultMap.remove(key);
                            log.warn("已清除key为【{}】的响应... ",key);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }, 0, RpcConstants.CACHERESULT_SCAN_INTERNAL, TimeUnit.MILLISECONDS);
    }


    /**
     * @author: qiu
     * @date: 2024/3/12 17:52
     * @description: 缓存响应
     */
    public void put(CacheResultKey key, T value) {
        writeLock.lock();
        try {
            cacheResultMap.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/12 17:52
     * @description: 获取缓存的响应
     */
    public T get(CacheResultKey key) {
        return cacheResultMap.get(key);
    }
}
