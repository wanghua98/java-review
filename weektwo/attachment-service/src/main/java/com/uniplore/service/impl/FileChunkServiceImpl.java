package com.uniplore.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileChunkMapper;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.mapper.FileUploadTaskMapper;
import com.uniplore.pojo.FileChunk;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;
import com.uniplore.result.ResultMessage;
import com.uniplore.service.FileChunkService;
import com.uniplore.util.CacheUtil;
import com.uniplore.util.FileHashUtil;
import com.uniplore.util.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;


/**
 * 文件分块Service实现类
 *
 * @author yf
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileChunkServiceImpl extends ServiceImpl<FileChunkMapper, FileChunk> implements FileChunkService {

    /**
     * 文件上传任务Mapper
     */
    private final FileUploadTaskMapper fileUploadTaskMapper;

    /**
     * 文件信息Mapper
     */
    private final FileInfoMapper fileInfoMapper;

    /**
     * 缓存工具
     */
    private final CacheUtil cacheUtil;

    /**
     * 上传文件物理存储路径
     */
    @Value("${file.upload-path}")
    private String path;

    /**
     * 线程池，用于异步处理分片合并计算SHA256操作
     *
     */
    private final ExecutorService executorService = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 分片合并锁工具
     */
    private final RedisLock redisLock;

    /**
     * 保存分片
     * <p>
     * 接收单个分片文件，保存到本地临时目录并记录分片信息。
     * 当所有分片上传完成后，自动触发合并操作。
     * </p>
     *
     * @param fileChunk 分片文件元信息（任务ID、分片序号等）
     * @param file      分片文件本体
     * @return 统一响应结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveChunk(FileChunk fileChunk, MultipartFile file) throws IOException, NoSuchAlgorithmException {

        // TODO 1.任务初始化上传时创建任务并缓存到redis 2. 分片上传时去redis中校验任务是否存在
        //  校验使用redis进行缓存分片信息也存储在redis中

        // 1. 校验上传任务是否存在且处于"上传中"状态
        FileUploadTask fileUploadTask = fileUploadTaskMapper.selectById(fileChunk.getTaskId());
        if (fileUploadTask == null) {
            return Result.error(500, ResultMessage.TASK_NOT_FOUND.getMessage(), null);
        }
        if (fileUploadTask.getStatus() != 0) {
            return Result.error(500, ResultMessage.TASK_NOT_IN_PROGRESS.getMessage(), null);
        }

        // 2. 创建分片存储目录（以任务ID命名），若已存在则跳过
        String chunkDirPath = this.path + File.separator + fileChunk.getTaskId();
        File directory = new File(chunkDirPath);
        if (!directory.exists() && !directory.mkdir()) {
            return Result.error(500, ResultMessage.CREATE_DIRECTORY_FAILED.getMessage(), null);
        }

        // 3. Redis 判断该分片是否已经上传过，避免重复写入 TODO当前判断还有问题，需要优化
        if (cacheUtil.isChunkUploaded(fileChunk.getTaskId(), fileChunk.getChunkNumber())) {
            log.info("分片已上传，跳过: taskId={}, chunkNumber={}", fileChunk.getTaskId(), fileChunk.getChunkNumber());
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }

        // 4. 将分片文件写入磁盘，文件名格式为 "{分片序号}.part"
        try {
            file.transferTo(new File(chunkDirPath + File.separator + fileChunk.getChunkNumber() + ".part"));
        } catch (Exception e) {
            return Result.error(500, ResultMessage.SAVE_CHUNK_FAILED.getMessage(), null);
        }

        // 5. 分片信息暂存到 Redis Hash（合并前不再写数据库）
        fileChunk.setChunkPath(fileChunk.getChunkNumber() + ".part");
        cacheUtil.putChunkInfo(fileChunk.getTaskId(), fileChunk);

        // 7. 更新上传任务的已上传分片计数
        fileUploadTaskMapper.update(null, new LambdaUpdateWrapper<FileUploadTask>().eq(FileUploadTask::getId, fileChunk.getTaskId()).setSql("uploaded_count = uploaded_count + 1"));

        // 8. 重新查询任务，判断是否所有分片均已上传完成
        fileUploadTask = fileUploadTaskMapper.selectById(fileChunk.getTaskId());
        if (fileUploadTask.getUploadedCount().equals(fileUploadTask.getChunkCount())) {
            // 全部分片上传完毕，将任务状态置为"上传完成"
            fileUploadTask.setStatus(1);
            fileUploadTaskMapper.updateById(fileUploadTask);
        }

        // 9. 若尚未全部上传完成，直接返回成功
        if (fileUploadTask.getStatus() != 1) {
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }

        // 10. 所有分片已就绪，执行异步合并操作，合并后计算SHA256哈希值
        String lockKey = "merge_chunks_lock:" + fileChunk.getTaskId();
        String id = UUID.randomUUID().toString().substring(0, 8) + Thread.currentThread().getId();
        // 获取锁
        boolean isLocked = redisLock.lock(lockKey, id);
        if (isLocked) {
            // 提交异步合并 + 算哈希值
            FileUploadTask finalFileUploadTask = fileUploadTask;
            CompletableFuture.runAsync(() -> {
                try {
                    // 1. 从 Redis Hash 读取全部分片信息，批量写入数据库
                    List<FileChunk> chunkInfos = cacheUtil.getChunkInfos(fileChunk.getTaskId());
                    if (!chunkInfos.isEmpty()) {
                        saveBatch(chunkInfos);
                    }
                    // 2. 清理 Redis 缓存
                    cacheUtil.deleteChunkInfos(fileChunk.getTaskId());

                    // 3. 合并分片
                    String fileName = mergeChunks(fileChunk.getTaskId());
                    if (fileName == null) {
                        throw new RuntimeException("合并分片失败");
                    }
                    String finalFilePath = chunkDirPath + File.separator + fileName;
                    // 计算hash
                    String sha256 = FileHashUtil.sha256(finalFilePath);
                    // 重新计算文件大小
                    long fileSize = FileHashUtil.fileSize(finalFilePath);
                    FileInfo fileInfo = new FileInfo();
                    // 文件信息字段
                    fileInfo.setFileName(finalFileUploadTask.getFileName());
                    fileInfo.setFileSuffix(finalFileUploadTask.getFileSuffix());
                    fileInfo.setFileSha256(sha256);
                    fileInfo.setFileSize(fileSize);
                    fileInfo.setStoragePath(finalFileUploadTask.getId() + File.separator + fileName);
                    fileInfo.setParentId(finalFileUploadTask.getParentId());
                    fileInfo.setCreateUser(finalFileUploadTask.getCreateUser());
                    fileInfo.setStatus(1);
                    fileInfoMapper.insert(fileInfo);

                    // 更新上传任务状态为"合并完成"
                    finalFileUploadTask.setStatus(2);
                    fileUploadTaskMapper.updateById(finalFileUploadTask);
                } catch (Exception e) {
                    log.error("合并分片失败，任务ID: {}, 错信息: {}", fileChunk.getTaskId(), e.getMessage(), e);
                } finally {
                    redisLock.unlock(lockKey, id);
                }
            }, executorService);
        }

        return Result.success(ResultMessage.CHUNK_MERGED_SUCCESS.getMessage());
    }

    /**
     * 合并分片
     * <p>
     * 将指定任务下的所有 {@code .part} 分片文件按序号拼接为完整文件，
     * 合并后的文件保存在分片目录内，以原始文件名命名。
     * </p>
     *
     * @param taskId 文件上传任务ID
     * @return 合并成功返回 true，失败返回 false
     */
    public String mergeChunks(Long taskId) throws IOException {
        // 分片临时存储目录
        String tempDirPath = this.path + File.separator + taskId;

        // 查询上传任务以获取原始文件名，用于命名合并后的完整文件
        FileUploadTask task = fileUploadTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("上传任务不存在: {}", taskId);
            return null;
        }
        // 合并后的完整文件路径 = 分片目录 + 随机数
        String fileName = RandomUtil.randomNumbers(5);
        String finalFilePath = tempDirPath + File.separator + fileName;

        // 校验分片目录是否存在
        File chunkDir = new File(tempDirPath);
        if (!chunkDir.exists() || !chunkDir.isDirectory()) {
            log.error("分片目录不存在或不是目录: {}", tempDirPath);
            return null;
        }

        // 列出所有 .part 分片文件
        File[] chunks = chunkDir.listFiles((dir, name) -> name.endsWith(".part"));
        if (chunks == null || chunks.length == 0) {
            log.error("分片目录下无分片文件: {}", tempDirPath);
            return null;
        }

        // 按分片序号升序排列，确保拼接顺序正确
        Arrays.sort(chunks, Comparator.comparingInt(f -> Integer.parseInt(f.getName().replace(".part", ""))));

        // 依次读取每个分片并追加写入到目标文件,try-with-resources 确保资源自动关闭
        try (FileOutputStream fos = new FileOutputStream(finalFilePath); FileChannel outChannel = fos.getChannel()) {
            for (File chunk : chunks) {
                try (FileInputStream fis = new FileInputStream(chunk); FileChannel inChannel = fis.getChannel()) {
                    long size = inChannel.size();
                    long transferred = 0;
                    // transferTo 可能不会一次传完，需要循环直到全部传输完成
                    while (transferred < size) {
                        transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
                    }
                }
            }
        } catch (IOException e) {
            log.error("合并文件失败: {}", finalFilePath, e);
            // 合并失败时清理可能产生的不完整文件
            File partialFile = new File(finalFilePath);
            if (partialFile.exists() && !partialFile.delete()) {
                log.error("删除不完整的合并文件失败: {}", finalFilePath);
            }
            throw e;
        }
        return fileName;
    }
}
