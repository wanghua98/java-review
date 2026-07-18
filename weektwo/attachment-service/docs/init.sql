-- =====================================================
-- 附件文件管理系统 数据库初始化脚本
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- Table: sys_user (用户信息表)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
                            `password`    VARCHAR(100) NOT NULL                COMMENT '密码（BCrypt加密存储）',
                            `nickname`    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
                            `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色（ADMIN：管理员，USER：普通用户）',
                            `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（1：正常，0：禁用）',
                            `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引',
                            KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';


-- -----------------------------------------------------
-- Table: file_directory (目录表)
-- 采用邻接表模型，parent_id=0 表示根目录
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_directory`;
CREATE TABLE `file_directory` (
                                  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '目录ID',
                                  `parent_id`   BIGINT       NOT NULL DEFAULT 0       COMMENT '父目录ID，0表示根目录',
                                  `name`        VARCHAR(255) NOT NULL                 COMMENT '目录名称',
                                  `sort`        INT          NOT NULL DEFAULT 0       COMMENT '排序号（数值越小越靠前）',
                                  `create_user` BIGINT       NOT NULL                 COMMENT '创建用户ID',
                                  `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态（1：正常，0：已删除）',
                                  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_parent_id` (`parent_id`) COMMENT '父目录索引',
                                  KEY `idx_create_user` (`create_user`) COMMENT '创建用户索引',
                                  KEY `idx_parent_user_name` (`parent_id`, `create_user`, `name`) COMMENT '同级目录重名校验索引（应用层配合检查status）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='目录表（支持无限层级）';


-- -----------------------------------------------------
-- Table: file_upload_task (文件上传任务表)
-- 记录大文件分片上传过程中的任务状态
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_upload_task`;
CREATE TABLE `file_upload_task` (
                                    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '上传任务ID',
                                    `file_sha256`    VARCHAR(64)                  COMMENT '文件SHA-256（用于秒传校验及唯一标识）',
                                    `file_name`      VARCHAR(255) NOT NULL                 COMMENT '文件名称',
                                    `file_size`      BIGINT       NOT NULL                 COMMENT '文件大小（单位：字节）',
                                    `file_suffix`    VARCHAR(20)  DEFAULT NULL             COMMENT '文件后缀（不含点）',
                                    `chunk_count`    INT          NOT NULL                 COMMENT '文件总分片数',
                                    `uploaded_count` INT          NOT NULL DEFAULT 0       COMMENT '已上传分片数',
                                    `status`         TINYINT      NOT NULL DEFAULT 0       COMMENT '上传状态（0：上传中，1：上传完成，2：上传失败）',
                                    `parent_id`      BIGINT       NOT NULL DEFAULT 0       COMMENT '目标目录ID，0表示根目录',
                                    `create_user`    BIGINT       NOT NULL                 COMMENT '创建用户ID',
                                    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_create_user` (`create_user`) COMMENT '用户索引',
                                    KEY `idx_status` (`status`) COMMENT '上传状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件上传任务表';


-- -----------------------------------------------------
-- Table: file_chunk (文件分片信息表)
-- 记录每个上传任务对应的分片临时文件信息
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_chunk`;
CREATE TABLE `file_chunk` (
                              `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '分片ID',
                              `task_id`      BIGINT        NOT NULL                  COMMENT '上传任务ID（关联file_upload_task.id）',
                              `chunk_number` INT           NOT NULL                  COMMENT '分片编号（从1开始）',
                              `chunk_path`   VARCHAR(500)  NOT NULL                  COMMENT '分片临时存储路径',
                              `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_task_chunk` (`task_id`, `chunk_number`) COMMENT '任务内分片编号唯一索引',
                              KEY `idx_task_id` (`task_id`) COMMENT '任务ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分片信息表';


-- -----------------------------------------------------
-- Table: file_info (文件信息表)
-- 保存上传完成后的正式文件记录，支持按目录查询、秒传复用
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
                             `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文件ID',
                             `file_name`    VARCHAR(255)  NOT NULL                 COMMENT '文件名称',
                             `file_size`    BIGINT        NOT NULL                 COMMENT '文件大小（单位：字节）',
                             `file_suffix`  VARCHAR(20)   DEFAULT NULL             COMMENT '文件后缀（不含点）',
                             `content_type` VARCHAR(100)  DEFAULT NULL             COMMENT 'MIME类型',
                             `file_sha256`  VARCHAR(64)                            COMMENT '文件SHA-256（用于秒传及完整性校验）',
                             `storage_path` VARCHAR(500)  NOT NULL                 COMMENT '文件存储路径',
                             `parent_id`    BIGINT        NOT NULL DEFAULT 0       COMMENT '所在目录ID，0表示根目录',
                             `create_user`  BIGINT        NOT NULL                 COMMENT '上传用户ID',
                             `status`       TINYINT       NOT NULL DEFAULT 1       COMMENT '文件状态（1：正常，0：已删除）',
                             `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传完成时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_file_sha256` (`file_sha256`) COMMENT 'SHA-256索引（秒传查询）',
                             KEY `idx_create_user` (`create_user`) COMMENT '用户索引',
                             KEY `idx_parent_id` (`parent_id`) COMMENT '目录索引',
                             KEY `idx_status` (`status`) COMMENT '状态索引',
                             KEY `idx_user_parent` (`create_user`, `parent_id`) COMMENT '用户+目录组合索引（优化目录文件列表查询）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件信息表（正式文件记录）';


-- -----------------------------------------------------
-- Table: file_share (文件分享表)
-- 原始分享令牌只返回给创建者，数据库仅保存 SHA-256 摘要
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share` (
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


-- -----------------------------------------------------
-- Table: file_operation_log (文件操作日志表)
-- 记录用户对文件和目录的操作行为，用于审计与追踪
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_operation_log`;
CREATE TABLE `file_operation_log` (
                                      `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                      `file_id`        BIGINT        DEFAULT NULL            COMMENT '关联文件ID（目录操作可为空）',
                                      `user_id`        BIGINT        NOT NULL                COMMENT '操作用户ID',
                                      `operation_type` VARCHAR(20)   NOT NULL                COMMENT '操作类型（UPLOAD/DOWNLOAD/DELETE/MOVE/RENAME/DIR_CREATE/DIR_DELETE/DIR_MOVE）',
                                      `description`    VARCHAR(500)  DEFAULT NULL            COMMENT '操作描述',
                                      `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_file_id` (`file_id`) COMMENT '文件ID索引',
                                      KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
                                      KEY `idx_create_time` (`create_time`) COMMENT '操作时间索引',
                                      KEY `idx_type_time` (`operation_type`, `create_time`) COMMENT '操作类型+时间组合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件操作日志表';


SET FOREIGN_KEY_CHECKS = 1;
