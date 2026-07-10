package com.uniplore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;

/**
 * 文件上传任务Service接口
 *
 * @author yf
 */
public interface FileUploadTaskService extends IService<FileUploadTask> {
    /**
     * 初始化文件上传任务
     *
     * @param fileUploadTask 文件上传任务信息
     * @return 结果
     */
    Result<FileUploadTask> initFile(FileUploadTask fileUploadTask);

}
