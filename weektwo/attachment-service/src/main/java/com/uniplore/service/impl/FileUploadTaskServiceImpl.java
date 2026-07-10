package com.uniplore.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileUploadTaskMapper;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;
import com.uniplore.service.FileUploadTaskService;
import org.springframework.stereotype.Service;

/**
 * 文件上传任务Service实现类
 *
 * @author yf
 */
@Service
public class FileUploadTaskServiceImpl extends ServiceImpl<FileUploadTaskMapper, FileUploadTask> implements FileUploadTaskService {

    /**
     * 初始化文件上传任务
     *
     * @param fileUploadTask 文件上传任务信息
     * @return 结果
     */
    @Override
    public Result<FileUploadTask> initFile(FileUploadTask fileUploadTask) {
        // 先判断文件是否已存在
        FileUploadTask existingTask = getOne(new QueryWrapper<FileUploadTask>().eq("file_md5", fileUploadTask.getFileMd5()));
        if (existingTask != null) {
            //TODO 文件已经存在可以秒传
            return Result.success(existingTask);
        }

        // 创建文件上传任务
        fileUploadTask.setCreateUser(StpUtil.getLoginIdAsLong());
        fileUploadTask.setStatus(0);
        fileUploadTask.setUploadedCount(0);
        save(fileUploadTask);

        // 返回文件上传任务信息 主要是返回生成id
        return Result.success(getOne(new QueryWrapper<FileUploadTask>().eq("file_md5", fileUploadTask.getFileMd5())));
    }
}
