package com.uniplore.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件分享记录。数据库只保存分享令牌摘要，不保存可直接使用的原始令牌。
 */
@Data
@NoArgsConstructor
@TableName("file_share")
public class FileShare {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private String tokenHash;
    private Long createUser;
    private LocalDateTime expiresAt;
    /** 1：有效，0：已撤销。 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
