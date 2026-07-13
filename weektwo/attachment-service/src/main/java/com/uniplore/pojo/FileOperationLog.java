package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * file_operation_log表对应的实体类（文件操作日志表）
 *
 * @author yf
 */
@TableName("file_operation_log")
@Data
@NoArgsConstructor
public class
FileOperationLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联文件ID（目录操作可为空）
     */
    private Long fileId;
    /**
     * 操作用户ID
     */
    private Long userId;
    /**
     * 操作类型（UPLOAD/DOWNLOAD/DELETE/MOVE/RENAME/DIR_CREATE/DIR_DELETE/DIR_MOVE）
     */
    private String operationType;
    /**
     * 操作描述
     */
    private String description;
    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
