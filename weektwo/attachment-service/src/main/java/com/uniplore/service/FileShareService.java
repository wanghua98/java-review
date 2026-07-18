package com.uniplore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.FileShare;
import com.uniplore.pojo.FileShareVO;
import com.uniplore.pojo.PublicFileShareVO;

import java.util.List;
import java.util.Optional;

public interface FileShareService extends IService<FileShare> {

    FileShareVO createShare(Long fileId, Integer expiresInHours, Long userId);

    List<FileShareVO> listUserShares(Long userId);

    void revokeShare(Long shareId, Long userId);

    Optional<ResolvedShare> resolveActiveShare(String rawToken);

    record ResolvedShare(FileShare share, com.uniplore.pojo.FileInfo fileInfo) {
        public PublicFileShareVO toPublicView() {
            return new PublicFileShareVO(
                    fileInfo.getFileName(),
                    fileInfo.getFileSize(),
                    fileInfo.getFileSuffix(),
                    share.getExpiresAt());
        }
    }
}
