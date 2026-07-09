package com.uniplore;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

    /**
     * 测试线程锁
     */
    @Test
    public void testThreadLock() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Executor executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    try {
                        Thread.sleep(10);
                        System.out.println(Thread.currentThread().getName() + " is running");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
        // 关闭线程池
        ((ThreadPoolExecutor) executor).shutdown();
        // 等待线程池中的任务执行完毕
        ((ThreadPoolExecutor) executor).awaitTermination(1, TimeUnit.MINUTES);
    }

}
