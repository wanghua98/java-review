package com.uniplore.pojo;

import java.time.Instant;

/** 短期文件预览票据响应。 */
public record PreviewTicketVO(String previewUrl, Instant expiresAt) {
}
