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
    INVALID_PARAMETERS("参数有误");

    /**
     * 消息文本
     */
    private final String message;
}
