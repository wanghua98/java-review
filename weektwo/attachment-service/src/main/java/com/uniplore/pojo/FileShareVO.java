package com.uniplore.pojo;

import java.time.LocalDateTime;

/** 文件所有者看到的分享信息。 */
public record FileShareVO(
        Long id,
        Long fileId,
        String fileName,
        String sharePath,
        LocalDateTime expiresAt,
        String status,
        LocalDateTime createTime
) {
}
