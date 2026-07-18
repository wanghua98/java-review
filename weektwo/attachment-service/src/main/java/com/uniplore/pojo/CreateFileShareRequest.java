package com.uniplore.pojo;

/** 创建文件分享请求。 */
public record CreateFileShareRequest(Long fileId, Integer expiresInHours) {
}
