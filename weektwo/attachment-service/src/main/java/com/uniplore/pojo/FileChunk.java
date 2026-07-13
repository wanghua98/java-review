package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * file_chunk表对应的实体类
 *
 * @author yf
 */
@TableName("file_chunk")
@Data
@NoArgsConstructor
public class FileChunk {
    /**
     * 分片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 上传任务ID
     */
    private Long taskId;
    /**
     * 分片编号（从1开始）
     */
    private Integer chunkNumber;
    /**
     * 分片临时存储路径
     */
    private String chunkPath;
    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
