package com.uniplore;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Java多线程学习示例类
 * 演示多线程的核心概念和使用方法
 *
 * @author 杨锋
 */
public class MultithreadingStudy {

    public static void main(String[] args) {
        System.out.println("=== Java多线程学习示例 ===\n");

        // 1. 创建线程的方式
        demonstrateThreadCreation();

        // 2. 线程同步
        demonstrateSynchronization();

        // 3. 线程通信
        demonstrateThreadCommunication();

        // 4. 线程池
        demonstrateThreadPool();
    }

    /**
     * 演示创建线程的三种方式
     */
    private static void demonstrateThreadCreation() {
        System.out.println("--- 1. 创建线程的方式 ---");

        // 方式1: 继承Thread类
        Thread thread1 = new MyThread("Thread-1");
        thread1.start();

        // 方式2: 实现Runnable接口
        Thread thread2 = new Thread(new MyRunnable(), "Thread-2");
        thread2.start();

        // 方式3: 使用Lambda表达式
        Thread thread3 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + " - Lambda方式: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Thread-3");
        thread3.start();

        // 等待所有线程完成
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * 演示线程同步
     */
    private static void demonstrateSynchronization() {
        System.out.println("--- 2. 线程同步 ---");

        Counter counter = new Counter();

        // 创建多个线程同时操作计数器
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    // 线程安全的计数操作
                    counter.increment();
                }
            }, "Sync-Thread-" + (i + 1));
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("最终计数值: " + counter.getCount());
        System.out.println();
    }

    /**
     * 演示线程通信（生产者-消费者模式）
     */
    private static void demonstrateThreadCommunication() {
        System.out.println("--- 3. 线程通信（生产者-消费者）---");

        Buffer buffer = new Buffer();

        // 生产者线程
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                buffer.produce(i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Producer");

        // 消费者线程
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                buffer.consume();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * 演示线程池的使用
     */
    private static void demonstrateThreadPool() {
        System.out.println("--- 4. 线程池 ---");

        ExecutorService executor = newFixedThreadPool(3);

        // 提交任务到线程池
        for (int i = 1; i <= 6; i++) {
            int taskId = i;
            executor.submit(() -> task(taskId));
        }

        // 关闭线程池
        executor.shutdown();
        try {
            executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("线程池关闭");
        }
        System.out.println();
    }

    public static void task(int taskId) {
        System.out.println(Thread.currentThread().getName() +
                " 正在执行任务 " + taskId);
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() +
                " 完成任务 " + taskId);
    }

    // ==================== 辅助类 ====================

    /**
     * 方式1: 继承Thread类
     */
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(getName() + " - 继承Thread: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 方式2: 实现Runnable接口
     */
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() +
                        " - 实现Runnable: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 线程安全的计数器
     */
    static class Counter {
        private int count = 0;

        // 使用synchronized关键字保证线程安全
        public synchronized void increment() {
            System.out.println(Thread.currentThread().getName() +
                    " 正在进行计数操作...");
            count++;
        }

        private final Lock lock = new ReentrantLock();

        // 使用Lock对象保证线程安全
        public void incr() {
            // 访问count时，需要加锁
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            // 访问count时，需要加锁
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 缓冲区类，用于演示线程通信
     */
    static class Buffer {
        private int data;
        private boolean available = false;

        // 生产者方法
        public synchronized void produce(int value) {
            while (available) {
                try {
                    wait(); // 缓冲区有数据，等待消费者消费
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            data = value;
            available = true;
            System.out.println(Thread.currentThread().getName() +
                    " 生产了数据: " + value);
            notify(); // 通知消费者可以消费了
        }

        // 消费者方法
        public synchronized void consume() {
            while (!available) {
                try {
                    wait(); // 缓冲区没有数据，等待生产者生产
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() +
                    " 消费了数据: " + data);
            available = false;
            notify(); // 通知生产者可以生产了
        }
    }
}
