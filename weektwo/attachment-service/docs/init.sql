-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    id BIGINT PRIMARY KEY COMMENT '用户ID',

    username VARCHAR(50) NOT NULL COMMENT '用户名',

    password VARCHAR(100) NOT NULL COMMENT '密码',

    nickname VARCHAR(50) COMMENT '昵称',

    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',

    status TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',

    create_time DATETIME COMMENT '创建时间',

    update_time DATETIME COMMENT '更新时间'
)ENGINE=InnoDB
 DEFAULT CHARSET=utf8mb4
 COMMENT='用户表';

-- 插入测试数据
INSERT INTO sys_user (id, username, password, nickname, role, status, create_time, update_time)
VALUES (1, 'admin', '$2a$10$tJfiFkzN.wSXrMYz8YNty.5.4y3g45WQm/orB4ceVrHk1j795ww9K', '管理员', 'ADMIN', 1, NOW(), NOW());


DROP TABLE IF EXISTS `file_upload_task`;
-- 文件上传任务表
CREATE TABLE `file_upload_task`
(
    `id`               BIGINT      NOT NULL COMMENT '上传任务ID',
    `file_md5`         VARCHAR(64) NOT NULL COMMENT '文件MD5，用于秒传及唯一标识',
    `file_name`       VARCHAR(255)  NOT NULL COMMENT '文件名称',
    `file_size`       BIGINT        NOT NULL COMMENT '文件大小（Byte）',
    `file_suffix`     VARCHAR(20)   NOT NULL COMMENT '文件后缀',
    `chunk_count`      INT         NOT NULL COMMENT '文件总分片数',
    `uploaded_count`   INT         NOT NULL DEFAULT 0 COMMENT '已上传分片数',
    `status`           TINYINT     NOT NULL DEFAULT 0 COMMENT '上传状态（0：上传中，1：上传完成，2：上传失败）',
    `create_user`      BIGINT      NOT NULL COMMENT '创建用户ID',
    `create_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_md5` (`file_md5`),
    KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='文件上传任务表';


DROP TABLE IF EXISTS `file_chunk`;
-- 文件分片信息表
CREATE TABLE `file_chunk`
(
    `id`            BIGINT       NOT NULL COMMENT '分片ID',
    `task_id`       BIGINT       NOT NULL COMMENT '上传任务ID',
    `chunk_number`  INT          NOT NULL COMMENT '分片编号（从1开始）',
    `chunk_path`    VARCHAR(500) NOT NULL COMMENT '分片临时存储路径',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_task_chunk` (`task_id`,`chunk_number`),

    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='文件分片信息表';

DROP TABLE IF EXISTS `file_info`;
-- 文件信息表
CREATE TABLE `file_info`
(
    `id`              BIGINT        NOT NULL COMMENT '文件ID',
    `file_name`       VARCHAR(255)  NOT NULL COMMENT '文件名称',
    `file_size`       BIGINT        NOT NULL COMMENT '文件大小（Byte）',
    `file_suffix`     VARCHAR(20)   NOT NULL COMMENT '文件后缀',
    `content_type`    VARCHAR(100)  NOT NULL COMMENT 'MIME类型',
    `file_md5`        VARCHAR(64)   NOT NULL COMMENT '文件MD5',
    `storage_path`    VARCHAR(500)  NOT NULL COMMENT '文件存储路径',
    `create_user`     BIGINT        NOT NULL COMMENT '上传用户ID',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '文件状态（1：正常，0：已删除）',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传完成时间',

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_file_md5` (`file_md5`),

    KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='文件信息表';