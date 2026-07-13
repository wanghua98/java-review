package com.uniplore.util;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;

/**
 * 文件分片测试
 */
public class FileChunkerUtil {

    /** 默认分片大小：5MB */
    private static final long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024L;

    /**
     * 将文件按指定大小分片
     *
     * @param filePath  待分片的文件路径
     * @param chunkSize 分片大小（字节），传 0 使用默认 5MB
     * @param outputDir 分片输出目录
     */
    public static void split(String filePath, long chunkSize, String outputDir) throws IOException {
        Path file = Paths.get(filePath);
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        if (chunkSize <= 0) chunkSize = DEFAULT_CHUNK_SIZE;
        long fileSize = Files.size(file);
        String taskId = RandomUtil.randomNumbers(10);
        Path chunkDir = Paths.get(outputDir, taskId);
        Files.createDirectories(chunkDir);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");
             FileChannel in = raf.getChannel()) {

            long pos = 0;
            int index = 0;
            while (pos < fileSize) {
                index++;
                long size = Math.min(chunkSize, fileSize - pos);
                Path chunk = chunkDir.resolve(index + ".part");

                try (FileChannel out = FileChannel.open(chunk,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    long transferred = 0;
                    while (transferred < size) {
                        transferred += in.transferTo(pos + transferred, size - transferred, out);
                    }
                }
                pos += size;
            }
            System.out.println("分片完成 → " + chunkDir + "，共 " + index + " 个分片");
        }
    }

    // ========== 测试 ==========

    @Test
    public void test() throws IOException {
        // 要分片的文件路径
        String filePath = "/Users/dao/dev/java-review/weektwo/attachment-service/src/main/resources/uploads/普罗米修斯.mov";

        // 分片大小（字节），默认 5MB 的话传 0
        long chunkSize = 40 * 1024 * 1024; // 或 10 * 1024 * 1024 表示 10MB

        // 输出目录
        String outputDir = "src/main/resources/uploads";

        split(filePath, chunkSize, outputDir);
    }
}
