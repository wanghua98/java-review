package com.uniplore.service;

import com.uniplore.config.PreviewProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * 生成和校验无状态、短期有效的文件访问票据。
 */
@Service
@RequiredArgsConstructor
public class SignedFileTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final PreviewProperties properties;

    @PostConstruct
    void validateConfiguration() {
        if (properties.getSigningSecret() == null || properties.getSigningSecret().length() < 32) {
            throw new IllegalStateException("preview.signing-secret 必须至少包含 32 个字符");
        }
    }

    public IssuedToken issue(Long fileId) {
        Instant expiresAt = Instant.now().plus(properties.getTicketTtl());
        byte[] nonce = new byte[16];
        SECURE_RANDOM.nextBytes(nonce);
        String payload = fileId + ":" + expiresAt.getEpochSecond() + ":"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sign(encodedPayload));
        return new IssuedToken("v1." + encodedPayload + "." + signature, expiresAt);
    }

    public Optional<Long> verify(String token) {
        if (token == null) {
            return Optional.empty();
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !"v1".equals(parts[0])) {
                return Optional.empty();
            }
            byte[] providedSignature = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(sign(parts[1]), providedSignature)) {
                return Optional.empty();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String[] fields = payload.split(":");
            if (fields.length != 3) {
                return Optional.empty();
            }
            long fileId = Long.parseLong(fields[0]);
            long expiresAt = Long.parseLong(fields[1]);
            if (fileId <= 0 || Instant.now().getEpochSecond() >= expiresAt) {
                return Optional.empty();
            }
            return Optional.of(fileId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("无法生成文件访问签名", e);
        }
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
