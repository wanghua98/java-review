package com.uniplore.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.mapper.FileUploadTaskMapper;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;
import com.uniplore.service.FileUploadTaskService;
import com.uniplore.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 文件上传任务Service实现类
 *
 * @author yf
 */
@Service
@RequiredArgsConstructor
public class FileUploadTaskServiceImpl extends ServiceImpl<FileUploadTaskMapper, FileUploadTask> implements FileUploadTaskService {

    private final FileInfoMapper fileInfoMapper;

    private final CacheUtil cacheUtil;

    /**
     * 初始化文件上传任务
     *
     * @param fileUploadTask 文件上传任务信息
     * @return 结果
     */
    @Override
    public Result<FileUploadTask> initFile(FileUploadTask fileUploadTask) {
        // 1. 如果有 SHA-256，检查文件是否已存在（秒传）
        String sha256 = fileUploadTask.getFileSha256();
        if (sha256 != null && !sha256.isEmpty()) {
            FileInfo existing = fileInfoMapper.selectOne(
                    new QueryWrapper<FileInfo>()
                            .eq("file_sha256", sha256)
                            .last("LIMIT 1")
            );
            if (existing != null) {
                // 文件已存在，直接创建一条新的文件记录引用同一存储文件，无需上传
                FileInfo newFileInfo = oldFileInfo(fileUploadTask, existing);
                fileInfoMapper.insert(newFileInfo);
                // 秒传成功，返回 -1 表示无需上传
                fileUploadTask.setId(-1L);
                return Result.success(fileUploadTask);
            }
        }

        // 2. 文件不存在（或无 SHA-256），创建上传任务走正常分片上传
        // 空字符串 SHA-256 转为 null，避免数据库索引冲突
        if (fileUploadTask.getFileSha256() != null && fileUploadTask.getFileSha256().isEmpty()) {
            fileUploadTask.setFileSha256(null);
        }
        fileUploadTask.setCreateUser(StpUtil.getLoginIdAsLong());
        fileUploadTask.setStatus(0);
        fileUploadTask.setUploadedCount(0);
        save(fileUploadTask);

        // 将分片总数写入 Redis，供 saveChunk 通过 Redis 判断是否全部上传完成
        cacheUtil.putTaskMeta(fileUploadTask.getId(), fileUploadTask.getChunkCount());

        return Result.success(getOne(new QueryWrapper<FileUploadTask>().eq("id", fileUploadTask.getId())));
    }

    /**
     * 从已存在的文件记录创建新的文件记录，引用同一存储文件
     * @param fileUploadTask 文件上传任务信息
     * @param existing       已存在的文件记录
     * @return 新的文件记录
     */
    @NonNull
    private static FileInfo oldFileInfo(FileUploadTask fileUploadTask, FileInfo existing) {
        FileInfo oldFileInfo = new FileInfo();
        oldFileInfo.setFileName(fileUploadTask.getFileName());
        oldFileInfo.setFileSize(existing.getFileSize());
        oldFileInfo.setFileSuffix(existing.getFileSuffix());
        oldFileInfo.setContentType(existing.getContentType());
        oldFileInfo.setFileSha256(existing.getFileSha256());
        oldFileInfo.setStoragePath(existing.getStoragePath());
        oldFileInfo.setParentId(fileUploadTask.getParentId());
        oldFileInfo.setCreateUser(StpUtil.getLoginIdAsLong());
        oldFileInfo.setStatus(1);
        return oldFileInfo;
    }
}
