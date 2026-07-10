package com.uniplore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 对于ConcurrentHashMap在并发情况下统计单词数量的一个小案例
 *
 * @author 杨锋
 * @date 2026/07/06
 */
public class CHMDemo {

    /**
     * 使用ConcurrentHashMap来统计单词数量
     */
    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    /**
     * 处理文件统计单词
     *
     * @param file 文件路径
     */
    public static void process(Path file) {
        // 处理文件
        try (var scanner = new Scanner(file)) {
        // 读取文件内容
            while (scanner.hasNext()) {
                // 获取单词
                String word = scanner.next();
                // 统计单词数量
                map.merge(word, 1L, Long::sum);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取目录下的所有文件和目录
     *
     * @param pathToRoot 目录路径
     * @return 所有文件和目录的集合
     * @throws IOException 如果IO操作失败
     */
    public static Set<Path> descents(Path pathToRoot) throws IOException {
        // return a set of all files and directories under pathToRoot
        try (Stream<Path> entries = Files.walk(pathToRoot)) {
            return entries.collect(Collectors.toSet());
        }
    }

    /**
     * 主方法
     *
     * @param args 命令行参数
     * @throws IOException          如果IO操作失败
     * @throws InterruptedException 如果线程被中断
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // 获取CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();
        // 创建一个固定大小的线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors,
                processors,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("my-thread" + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());


        Path pathToRoot = Path.of(".");
        // 遍历所有文件和目录
        for (Path p : descents(pathToRoot)) {
            executor.submit(() -> process(p));
        }

        // 关闭线程池，不再接收新任务，但已提交任务继续执行并且阻塞等待所有任务完成
        executor.shutdown(); //不再接收新任务，但已提交任务继续执行并且阻塞等待所有任务完成
        // 阻塞等待所有任务完成
        executor.awaitTermination(10, TimeUnit.MINUTES);

        // 遍历统计结果
        map.forEach((k, v) ->
        {
            if (v > 20) {
                System.out.println(k + ": " + v);
            }
        });

    }
}
