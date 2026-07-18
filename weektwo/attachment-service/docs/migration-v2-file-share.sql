CREATE TABLE IF NOT EXISTS `file_share` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    `file_id`     BIGINT      NOT NULL                COMMENT '文件ID',
    `token_hash`  CHAR(64)    NOT NULL                COMMENT '分享令牌SHA-256摘要',
    `create_user` BIGINT      NOT NULL                COMMENT '创建用户ID',
    `expires_at`  DATETIME    NOT NULL                COMMENT '过期时间',
    `status`      TINYINT     NOT NULL DEFAULT 1      COMMENT '状态（1有效，0撤销）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_share_token_hash` (`token_hash`),
    KEY `idx_share_owner_time` (`create_user`, `create_time`),
    KEY `idx_share_file` (`file_id`),
    KEY `idx_share_expiry` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分享表';
