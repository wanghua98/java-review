package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * file_directory表对应的实体类（目录表，支持无限层级）
 *
 * @author yf
 */
@TableName("file_directory")
@Data
@NoArgsConstructor
public class FileDirectory {
    /**
     * 目录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 父目录ID，0表示根目录
     */
    private Long parentId;
    /**
     * 目录名称
     */
    private String name;
    /**
     * 排序号（数值越小越靠前）
     */
    private Integer sort;
    /**
     * 创建用户ID
     */
    private Long createUser;
    /**
     * 状态（1：正常，0：已删除）
     */
    private Integer status;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
