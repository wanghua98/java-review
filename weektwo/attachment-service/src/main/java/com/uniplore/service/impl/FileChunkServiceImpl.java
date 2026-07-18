package com.uniplore.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.uniplore.service.FileDirectoryService;
import com.uniplore.util.CacheUtil;
import com.uniplore.util.FileHashUtil;
import com.uniplore.util.RedisLock;
import com.uniplore.util.AfterCommitRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
    private final ExecutorService executorService = new ThreadPoolExecutor(500, 500, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 分片合并锁工具
     */
    private final RedisLock redisLock;

    /**
     * 文件目录Service（用于自动重命名冲突文件名）
     */
    private final FileDirectoryService fileDirectoryService;

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
    public Result<String> saveChunk(FileChunk fileChunk, MultipartFile file) throws IOException {

        // 通过 Redis 校验上传任务是否存在
        if (!cacheUtil.isTaskExists(fileChunk.getTaskId())) {
            return Result.error(500, ResultMessage.TASK_NOT_FOUND.getMessage(), null);
        }

        // 创建分片存储目录（以任务ID命名），若已存在则跳过
        String chunkDirPath = this.path + File.separator + fileChunk.getTaskId();
        File directory = new File(chunkDirPath);
        if (!directory.exists() && !directory.mkdir()) {
            return Result.error(500, ResultMessage.CREATE_DIRECTORY_FAILED.getMessage(), null);
        }

        // Redis 判断该分片是否已经上传过，避免重复写入
        if (cacheUtil.isChunkUploaded(fileChunk.getTaskId(), fileChunk.getChunkNumber())) {
            log.info("分片已上传，跳过: taskId={}, chunkNumber={}", fileChunk.getTaskId(), fileChunk.getChunkNumber());
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }

        // 将分片文件写入磁盘，文件名格式为 "{分片序号}.part"
        try {
            file.transferTo(new File(chunkDirPath + File.separator + fileChunk.getChunkNumber() + ".part"));
        } catch (Exception e) {
            return Result.error(500, ResultMessage.SAVE_CHUNK_FAILED.getMessage(), null);
        }

        // 分片信息暂存到 Redis Hash合并前不再写数据库
        fileChunk.setChunkPath(fileChunk.getChunkNumber() + ".part");
        cacheUtil.putChunkInfo(fileChunk.getTaskId(), fileChunk);

        // 异步更新上传任务的已上传分片计数（仅做 DB 记录，合并条件由 Redis 判断）
        executorService.execute(() ->
                fileUploadTaskMapper.update(null,
                        new LambdaUpdateWrapper<FileUploadTask>()
                                .eq(FileUploadTask::getId, fileChunk.getTaskId())
                                .setIncrBy(FileUploadTask::getUploadedCount, 1)));


        // 通过 Redis 判断是否所有分片均已上传完成（HLEN == total）
        long uploadedCount = cacheUtil.getUploadedChunkCount(fileChunk.getTaskId());
        Integer totalCount = cacheUtil.getTaskChunkCount(fileChunk.getTaskId());

        // 若尚未全部上传完成，直接返回成功
        if (totalCount == null || uploadedCount < totalCount) {
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }

        // 全部分片已就绪，同步执行合并
        Long taskId = fileChunk.getTaskId();

        // 原子性检查：只有获得 Redis 锁的线程执行合并
        String lockKey = "merge:" + taskId;
        String lockId = UUID.randomUUID().toString().substring(0, 8) + Thread.currentThread().getId();
        // 尝试获取锁，若失败则直接返回成功
        if (!redisLock.lock(lockKey, lockId)) {
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }
        try {
            // 从 Redis Hash 读取全部分片信息，批量写入 file_chunk DB
            List<FileChunk> chunkInfos = cacheUtil.getChunkInfos(taskId);
            if (!chunkInfos.isEmpty()) {
                saveBatch(chunkInfos);
            }

            // 清理 Redis 分片缓存
            cacheUtil.deleteChunkInfos(taskId);

            // 查询任务信息
            FileUploadTask uploadTask = fileUploadTaskMapper.selectById(taskId);
            if (uploadTask == null) {
                return Result.error(500, "上传任务不存在", null);
            }

            // 合并 .part 文件
            String mergedName = mergeChunks(taskId);
            if (mergedName == null) {
                return Result.error(500, ResultMessage.MERGE_CHUNKS_FAILED.getMessage(), null);
            }

            // 插入 FileInfo（status=1：上传成功未计算hash）
            // 合并后的文件路径（任务ID/合并后的文件名）
            String fullPath = chunkDirPath + File.separator + mergedName;
            FileInfo fileInfo = new FileInfo();

            // 自动重命名：如果目标目录已存在同名文件，追加编号避免冲突
            String uniqueName = fileDirectoryService.resolveUniqueFileName(
                    uploadTask.getParentId(), uploadTask.getFileName());

            // 复制文件信息到 FileInfo
            BeanUtils.copyProperties(uploadTask, fileInfo);
            fileInfo.setFileName(uniqueName);
            fileInfo.setFileSize(FileHashUtil.fileSize(fullPath));
            fileInfo.setStoragePath(taskId + File.separator + mergedName);
            fileInfo.setStatus(1);
            fileInfo.setId(null);
            fileInfo.setFileSha256(null);
            fileInfoMapper.insert(fileInfo);

            // 更新任务 status=2（已合并待计算哈希）
            uploadTask.setStatus(2);
            fileUploadTaskMapper.updateById(uploadTask);

            // 等待文件信息插入成功后，异步计算 SHA-256 并与前端哈希比较验证是否损坏
            String finalStoragePath = taskId + File.separator + mergedName;
            AfterCommitRunner.afterCommit(() -> executorService.execute(() ->
                    computeAndVerifyHash(taskId, finalStoragePath, fullPath, uploadTask.getFileSha256())));

            return Result.success(ResultMessage.CHUNK_MERGED_SUCCESS.getMessage());
        } finally {
            redisLock.unlock(lockKey, lockId);
        }
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

    /**
     * 异步计算文件 SHA-256 并与前端传入的哈希比较
     * <p>
     * 哈希一致 → 更新 FileInfo.sha256，任务标记完成（status=3）
     * 哈希不一致 → 文件名追加"（文件损坏）"，任务标记失败（status=-1）
     * </p>
     *
     * @param taskId       上传任务ID
     * @param storagePath  文件存储路径
     * @param fullPath     文件完整物理路径
     * @param frontendHash 前端传入的 SHA-256（可为空）
     */
    private void computeAndVerifyHash(Long taskId, String storagePath, String fullPath, String frontendHash) {
        try {
            // 计算文件 SHA-256
            String computedHash = FileHashUtil.sha256(fullPath);

            // 查询 FileInfo
            FileInfo fileInfo = fileInfoMapper.selectOne(
                    new QueryWrapper<FileInfo>().eq("storage_path", storagePath)
            );
            if (fileInfo == null) {
                log.error("哈希校验：FileInfo 不存在，storagePath: {}", storagePath);
                return;
            }

            // 查询任务
            FileUploadTask task = fileUploadTaskMapper.selectById(taskId);
            if (task == null) {
                log.error("哈希校验：任务不存在，taskId: {}", taskId);
                return;
            }

            if (frontendHash == null || frontendHash.equals(computedHash)) {
                // 前端未传哈希或哈希一致，使用后端计算值
                fileInfo.setFileSha256(computedHash);
                fileInfoMapper.updateById(fileInfo);
                task.setStatus(3);
                log.info("文件哈希校验通过，taskId: {}, fileName: {}", taskId, fileInfo.getFileName());
            } else {
                // 前端传哈希且哈希不一致，文件名追加损坏标记
                log.warn("文件哈希校验不通过，taskId: {}, frontendHash: {}, computedHash: {}",
                        taskId, frontendHash, computedHash);
                fileInfo.setFileName(fileInfo.getFileName() + "（文件损坏）");
                fileInfoMapper.updateById(fileInfo);
                task.setStatus(-1);
            }
            fileUploadTaskMapper.updateById(task);

        } catch (Exception e) {
            log.error("哈希校验失败，taskId: {}", taskId, e);
        }
    }
}
