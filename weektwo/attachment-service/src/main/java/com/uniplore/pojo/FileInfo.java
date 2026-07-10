package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * file_info表对应的实体类
 *
 * @author yf
 */
@TableName("file_info")
@Data
@NoArgsConstructor
public class FileInfo {
    /**
     * 文件ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小（Byte）
     */
    private Long fileSize;
    /**
     * 文件后缀
     */
    private String fileSuffix;
    /**
     * MIME类型
     */
    private String contentType;
    /**
     * 文件MD5
     */
    private String fileMd5;
    /**
     * 文件存储路径
     */
    private String storagePath;
    /**
     * 上传用户ID
     */
    private Long createUser;
    /**
     * 文件状态（1：正常，0：已删除）
     */
    private Integer status;
    /**
     * 上传完成时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
