package com.uniplore.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件哈希工具类
 * <p>
 * 计算文件的 SHA-256 哈希值，使用内存映射方式处理大文件。
 * </p>
 *
 * <pre>{@code
 * String sha = FileHashUtil.sha256("/path/to/file.zip");
 * System.out.println(sha);
 * }</pre>
 *
 * @author yf
 */
public class FileHashUtil {

    private FileHashUtil() {
        // 工具类禁止实例化
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    public static long fileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }

    /**
     * 计算文件的 SHA-256 哈希值
     *
     * @param filePath 文件路径
     * @return SHA-256 十六进制字符串（小写）
     */
    public static String sha256(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileChannel channel = raf.getChannel()) {

            long size = channel.size();
            long position = 0;
            // 每次映射 100MB，避免超大文件导致 OOM
            long mapSize = Math.min(size, 100 * 1024 * 1024L);

            while (position < size) {
                long remaining = size - position;
                long actualMapSize = Math.min(mapSize, remaining);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, actualMapSize);
                digest.update(buffer);
                position += actualMapSize;
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder(hashBytes.length * 2);
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
