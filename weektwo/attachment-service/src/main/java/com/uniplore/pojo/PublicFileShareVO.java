package com.uniplore.pojo;

import java.time.LocalDateTime;

/** 访客通过分享链接可见的最小文件信息。 */
public record PublicFileShareVO(
        String fileName,
        Long fileSize,
        String fileSuffix,
        LocalDateTime expiresAt
) {
}
