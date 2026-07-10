package com.uniplore.controller;

import com.uniplore.pojo.FileChunk;
import com.uniplore.pojo.FileUploadTask;
import com.uniplore.result.Result;
import com.uniplore.service.FileChunkService;
import com.uniplore.service.FileUploadTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件相关接口控制器
 *
 * @author dao
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    /**
     * 文件分块服务
     */
    private final FileChunkService fileChunkService;

    /**
     * 文件任务
     */
    private final FileUploadTaskService fileUploadTaskService;



    /**
     * 文件初始化接口
     */
    @PostMapping("/init")
    public Result<FileUploadTask> initFile(@RequestBody FileUploadTask fileUploadTask) {
        if(fileUploadTask.getFileMd5() == null || fileUploadTask.getFileName() == null) {
            return Result.error(400, "参数有误", null);
        }
        return fileUploadTaskService.initFile(fileUploadTask);
    }


    /**
     * 上传分片接口
     *
     * @param taskId      任务ID
     * @param chunkNumber 分片编号
     * @param file        分片文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<String> uploadChunk(@RequestParam("taskId") String taskId,
                                      @RequestParam("chunkNumber") Integer chunkNumber,
                                      @RequestParam("file") MultipartFile file) throws IOException {

        //参数校验
        if (file == null || taskId == null || chunkNumber == null) {
            return Result.error(400, "参数有误", null);
        }

        // 将taskId转为Long，防止数值溢出
        Long parsedTaskId;
        try {
            parsedTaskId = Long.parseLong(taskId);
        } catch (NumberFormatException e) {
            return Result.error(400, "taskId格式错误: " + taskId, null);
        }

        // 创建分片文件信息对象
        FileChunk fileChunk = new FileChunk();
        // 设置分片文件信息
        fileChunk.setTaskId(parsedTaskId);
        fileChunk.setChunkNumber(chunkNumber);
        // 保存分片文件
        return fileChunkService.saveChunk(fileChunk, file);
    }
}
