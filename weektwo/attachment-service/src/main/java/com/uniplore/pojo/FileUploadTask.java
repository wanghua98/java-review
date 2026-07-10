package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * file_upload_task表对应的实体类
 *
 * @author yf
 */
@TableName("file_upload_task")
@Data
@NoArgsConstructor
public class FileUploadTask {
    /**
     * 上传任务ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 文件MD5，用于秒传及唯一标识
     */
    private String fileMd5;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件后缀
     */
    private String fileSuffix;
    /**
     * 文件总分片数
     */
    private Integer chunkCount;
    /**
     * 已上传分片数
     */
    private Integer uploadedCount;
    /**
     * 上传状态（0：上传中，1：上传完成，2：上传失败）
     */
    private Integer status;
    /**
     * 创建用户ID
     */
    private Long createUser;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
