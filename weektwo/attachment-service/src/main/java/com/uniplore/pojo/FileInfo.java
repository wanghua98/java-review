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
    @TableId(type = IdType.AUTO)
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
     * 文件SHA-256（用于秒传及完整性校验）
     */
    private String fileSha256;
    /**
     * 文件存储路径
     */
    private String storagePath;
    /**
     * 所在目录ID，0表示根目录
     */
    private Long parentId;
    /**
     * 上传用户ID
     */
    private Long createUser;
    /**
     * 文件状态（-1：文件损坏，0：已删除，1：上传成功未计算hash，3：计算完hash正常可用）
     */
    private Integer status;
    /**
     * 上传完成时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
