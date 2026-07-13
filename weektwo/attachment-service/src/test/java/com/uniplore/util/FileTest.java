package com.uniplore.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算文件SHA-256哈希值的最小示例
 */
public class FileTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        // 目标文件路径
        String path = "/Users/dao/dev/java-review/weektwo/attachment-service/src/main/resources/uploads/2075506768628174850/2.part";

        // 创建SHA-256摘要实例
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 使用内存映射方式读取文件，避免大文件OOM
        try (RandomAccessFile raf = new RandomAccessFile(path, "r");
             FileChannel channel = raf.getChannel()) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            digest.update(buffer);
        }

        // 将字节数组转为十六进制字符串
        byte[] hashBytes = digest.digest();
        StringBuilder sha = new StringBuilder(hashBytes.length * 2);
        for (byte b : hashBytes) {
            // 将字节转为十六进制字符串
            sha.append(String.format("%02x", b&0xff));
        }

        System.out.println("SHA-256: " + sha);
    }
}
