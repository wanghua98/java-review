package com.uniplore.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileChunkMapper;
import com.uniplore.mapper.FileUploadTaskMapper;
import com.uniplore.pojo.FileChunk;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;
import com.uniplore.result.ResultMessage;
import com.uniplore.service.FileChunkService;
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
import java.util.Arrays;
import java.util.Comparator;


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

    @Value("${file.upload-path}")
    private String path;


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

        // TODO: 使用 Redis 判断该分片是否已经上传过，避免重复写入

        // 3. 将分片文件写入磁盘，文件名格式为 "{分片序号}.part"
        try {
            file.transferTo(new File(chunkDirPath + File.separator + fileChunk.getChunkNumber() + ".part"));
        } catch (Exception e) {
            return Result.error(500, ResultMessage.SAVE_CHUNK_FAILED.getMessage(), null);
        }

        // 4. 持久化分片元信息到数据库
        fileChunk.setChunkPath(chunkDirPath + File.separator + fileChunk.getChunkNumber());
        if (!save(fileChunk)) {
            return Result.error(500, ResultMessage.SAVE_CHUNK_INFO_FAILED.getMessage(), null);
        }

        // 5. 更新上传任务的已上传分片计数
        fileUploadTaskMapper.update(null,
                new LambdaUpdateWrapper<FileUploadTask>()
                        .eq(FileUploadTask::getId, fileChunk.getTaskId())
                        .setSql("uploaded_count = uploaded_count + 1")
        );

        // 6. 重新查询任务，判断是否所有分片均已上传完成
        fileUploadTask = fileUploadTaskMapper.selectById(fileChunk.getTaskId());
        if (fileUploadTask.getUploadedCount().equals(fileUploadTask.getChunkCount())) {
            // 全部分片上传完毕，将任务状态置为"上传完成"
            fileUploadTask.setStatus(1);
            fileUploadTaskMapper.updateById(fileUploadTask);
        }

        // 7. 若尚未全部上传完成，直接返回成功
        if (fileUploadTask.getStatus() != 1) {
            return Result.success(ResultMessage.CHUNK_SAVED_SUCCESS.getMessage());
        }

        // 8. 所有分片已就绪，执行合并
        if (mergeChunks(fileChunk.getTaskId())) {
            return Result.success(ResultMessage.CHUNK_MERGED_SUCCESS.getMessage());
        } else {
            return Result.error(500, ResultMessage.MERGE_CHUNKS_FAILED.getMessage(), null);
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
    public boolean mergeChunks(Long taskId) throws IOException {
        // 分片临时存储目录
        String tempDirPath = this.path + File.separator + taskId;

        // 查询上传任务以获取原始文件名，用于命名合并后的完整文件
        FileUploadTask task = fileUploadTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("上传任务不存在: " + taskId);
            return false;
        }
        // 合并后的完整文件路径 = 分片目录 + 原始文件名
        String finalFilePath = tempDirPath + File.separator + task.getFileName();

        // 校验分片目录是否存在
        File chunkDir = new File(tempDirPath);
        if (!chunkDir.exists() || !chunkDir.isDirectory()) {
            log.error("分片目录不存在或不是目录: " + tempDirPath);
            return false;
        }

        // 列出所有 .part 分片文件
        File[] chunks = chunkDir.listFiles((dir, name) -> name.endsWith(".part"));
        if (chunks == null || chunks.length == 0) {
            log.error("分片目录下无分片文件: " + tempDirPath);
            return false;
        }

        // 按分片序号升序排列，确保拼接顺序正确
        Arrays.sort(chunks, Comparator.comparingInt(
                f -> Integer.parseInt(f.getName().replace(".part", ""))
        ));

        // 依次读取每个分片并追加写入到目标文件
        try (FileOutputStream fos = new FileOutputStream(finalFilePath);
             FileChannel outChannel = fos.getChannel()) {
            for (File chunk : chunks) {
                try (FileChannel inChannel = new FileInputStream(chunk).getChannel()) {
                    long size = inChannel.size();
                    long transferred = 0;
                    // transferTo 可能不会一次传完，需要循环直到全部传输完成
                    while (transferred < size) {
                        transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
                    }
                }
            }
        } catch (IOException e) {
            log.error("合并文件失败: " + finalFilePath, e);
            // 合并失败时清理可能产生的不完整文件
            File partialFile = new File(finalFilePath);
            if (partialFile.exists() && !partialFile.delete()) {
                log.error("删除不完整的合并文件失败: " + finalFilePath);
            }
            throw e;
        }
        return true;
    }
}
