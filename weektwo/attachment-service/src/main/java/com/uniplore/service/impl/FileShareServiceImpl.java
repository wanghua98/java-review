package com.uniplore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.mapper.FileShareMapper;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.FileShare;
import com.uniplore.pojo.FileShareVO;
import com.uniplore.service.FileShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements FileShareService {

    private static final int DEFAULT_EXPIRY_HOURS = 24;
    private static final int MAX_EXPIRY_HOURS = 24 * 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FileInfoMapper fileInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShareVO createShare(Long fileId, Integer expiresInHours, Long userId) {
        int expiryHours = expiresInHours == null ? DEFAULT_EXPIRY_HOURS : expiresInHours;
        if (expiryHours < 1 || expiryHours > MAX_EXPIRY_HOURS) {
            throw new IllegalArgumentException("分享有效期必须在 1 到 720 小时之间");
        }

        FileInfo fileInfo = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, fileId)
                .eq(FileInfo::getCreateUser, userId)
                .gt(FileInfo::getStatus, 0));
        if (fileInfo == null) {
            throw new IllegalArgumentException("文件不存在或无权分享");
        }

        String rawToken = newRawToken();
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setTokenHash(hashToken(rawToken));
        share.setCreateUser(userId);
        share.setExpiresAt(LocalDateTime.now().plusHours(expiryHours));
        share.setStatus(1);
        save(share);
        return toView(share, fileInfo.getFileName(), "/share/" + rawToken);
    }

    @Override
    public List<FileShareVO> listUserShares(Long userId) {
        List<FileShare> shares = list(new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getCreateUser, userId)
                .orderByDesc(FileShare::getCreateTime));
        if (shares.isEmpty()) {
            return List.of();
        }
        List<Long> fileIds = shares.stream().map(FileShare::getFileId).distinct().toList();
        Map<Long, FileInfo> files = fileInfoMapper.selectByIds(fileIds).stream()
                .collect(Collectors.toMap(FileInfo::getId, Function.identity()));
        return shares.stream()
                .map(share -> toView(
                        share,
                        Optional.ofNullable(files.get(share.getFileId()))
                                .map(FileInfo::getFileName).orElse("文件已删除"),
                        null))
                .toList();
    }

    @Override
    public void revokeShare(Long shareId, Long userId) {
        boolean updated = update(new LambdaUpdateWrapper<FileShare>()
                .eq(FileShare::getId, shareId)
                .eq(FileShare::getCreateUser, userId)
                .eq(FileShare::getStatus, 1)
                .set(FileShare::getStatus, 0));
        if (!updated) {
            throw new IllegalArgumentException("分享不存在、已失效或无权操作");
        }
    }

    @Override
    public Optional<ResolvedShare> resolveActiveShare(String rawToken) {
        if (rawToken == null || rawToken.length() < 32 || rawToken.length() > 128) {
            return Optional.empty();
        }
        FileShare share = getOne(new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getTokenHash, hashToken(rawToken))
                .eq(FileShare::getStatus, 1)
                .gt(FileShare::getExpiresAt, LocalDateTime.now())
                .last("LIMIT 1"));
        if (share == null) {
            return Optional.empty();
        }
        FileInfo fileInfo = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, share.getFileId())
                .gt(FileInfo::getStatus, 0));
        return fileInfo == null ? Optional.empty() : Optional.of(new ResolvedShare(share, fileInfo));
    }

    private static FileShareVO toView(FileShare share, String fileName, String sharePath) {
        String status;
        if (share.getStatus() == null || share.getStatus() != 1) {
            status = "REVOKED";
        } else if (!share.getExpiresAt().isAfter(LocalDateTime.now())) {
            status = "EXPIRED";
        } else {
            status = "ACTIVE";
        }
        return new FileShareVO(
                share.getId(), share.getFileId(), fileName, sharePath,
                share.getExpiresAt(), status, share.getCreateTime());
    }

    private static String newRawToken() {
        byte[] token = new byte[32];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private static String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", e);
        }
    }
}
