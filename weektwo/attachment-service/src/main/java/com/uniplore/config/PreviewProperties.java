package com.uniplore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 文件预览配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "preview")
public class PreviewProperties {

    /** HMAC 签名密钥，生产环境必须通过环境变量覆盖。 */
    private String signingSecret;

    /** kkFileView 获取原文件时使用的后端内部地址。 */
    private String sourceBaseUrl = "http://localhost:8001";

    /** 浏览器访问 kkFileView 时使用的统一入口。 */
    private String publicBaseUrl = "/preview";

    /** 单次预览票据有效期。 */
    private Duration ticketTtl = Duration.ofMinutes(2);
}
