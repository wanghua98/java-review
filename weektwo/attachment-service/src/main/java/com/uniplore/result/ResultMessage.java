package com.uniplore.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应消息枚举
 * <p>
 * 集中管理所有业务返回的消息文本，避免硬编码字符串散落在各处。
 * </p>
 *
 * @author yf
 */
@Getter
@AllArgsConstructor
public enum ResultMessage {

    /**
     * 上传任务不存在
     */
    TASK_NOT_FOUND("上传任务不存在"),

    /**
     * 上传任务状态不是"上传中"
     */
    TASK_NOT_IN_PROGRESS("上传任务当前不在上传中状态"),

    /**
     * 创建分片存储目录失败
     */
    CREATE_DIRECTORY_FAILED("创建分片存储目录失败"),

    /**
     * 保存分片文件失败
     */
    SAVE_CHUNK_FAILED("保存分片文件失败"),

    /**
     * 保存分片信息到数据库失败
     */
    SAVE_CHUNK_INFO_FAILED("保存分片信息失败"),

    /**
     * 合并分片失败
     */
    MERGE_CHUNKS_FAILED("合并分片失败"),

    /**
     * 分片保存成功（尚未全部上传完成）
     */
    CHUNK_SAVED_SUCCESS("分片保存成功"),

    /**
     * 分片保存成功且已完成合并
     */
    CHUNK_MERGED_SUCCESS("分片保存成功,后台正在进行合并"),

    /**
     * 用户未登录
     */
    USER_NOT_LOGGED_IN("用户未登录"),

    /**
     * 参数有误
     */
    INVALID_PARAMETERS("参数有误"),

    // ==================== 用户校验 ====================

    /**
     * 用户名为空
     */
    USERNAME_EMPTY("用户名不能为空"),

    /**
     * 用户名长度超过限制
     */
    USERNAME_TOO_LONG("用户名长度不能超过50个字符"),

    /**
     * 用户名格式错误（必须以字母开头）
     */
    USERNAME_MUST_START_WITH_LETTER("用户名必须以字母开头"),

    /**
     * 用户名包含非法字符
     */
    USERNAME_INVALID_CHARS("用户名只能包含字母和数字"),

    /**
     * 密码为空
     */
    PASSWORD_EMPTY("密码不能为空"),

    /**
     * 密码太短
     */
    PASSWORD_TOO_SHORT("密码长度不能少于2位"),

    /**
     * 密码太长
     */
    PASSWORD_TOO_LONG("密码长度不能超过100位"),

    // ==================== 目录/文件名校验 ====================

    /**
     * 目录名为空
     */
    DIR_NAME_EMPTY("目录名称不能为空"),

    /**
     * 目录名太长
     */
    DIR_NAME_TOO_LONG("目录名称长度不能超过100个字符"),

    /**
     * 目录名包含非法字符
     */
    DIR_NAME_INVALID_CHARS("目录名称包含非法字符: "),

    /**
     * 文件名为空
     */
    FILE_NAME_EMPTY("文件名不能为空"),

    /**
     * 文件名太长
     */
    FILE_NAME_TOO_LONG("文件名长度不能超过255个字符"),

    /**
     * 文件名包含非法字符
     */
    FILE_NAME_INVALID_CHARS("文件名包含非法字符: ");

    /**
     * 消息文本
     */
    private final String message;
}
