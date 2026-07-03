package com.uniplore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 对于ConcurrentHashMap在并发情况下统计单词数量的一个小案例
public class CHMDemo {

    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    public static void process(Path flie) {
        try (var in = new Scanner(flie)) {
            while (in.hasNext()) {
                String word = in.next();
                map.merge(word, 1L, Long::sum);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Path> descents(Path pathToRoot) throws IOException {
        // return a set of all files and directories under pathToRoot
        try (Stream<Path> entries = Files.walk(pathToRoot)) {
            return entries.collect(Collectors.toSet());
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        Path pathToRoot = Path.of(".");
        for (Path p : descents(pathToRoot))
        {
            executor.submit(() -> process(p));
        }

        executor.shutdown(); //不再接收新任务，但已提交任务继续执行并且阻塞等待所有任务完成
        executor.awaitTermination(10, TimeUnit.MINUTES); //阻塞等待所有任务完成

        map.forEach((k, v) ->
        {
            if (v > 10) {
                System.out.println(k + ": " + v);
            }
        });

    }
}
