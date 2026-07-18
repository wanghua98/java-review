package com.uniplore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.PreviewTicketVO;
import com.uniplore.pojo.PublicFileShareVO;
import com.uniplore.result.Result;
import com.uniplore.service.FileResourceService;
import com.uniplore.service.FileShareService;
import com.uniplore.service.PreviewUrlService;
import com.uniplore.service.SignedFileTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无需登录的受控文件入口：短期预览票据和随机分享令牌是唯一授权凭据。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicFileAccessController {

    private final SignedFileTokenService signedFileTokenService;
    private final FileShareService fileShareService;
    private final FileInfoMapper fileInfoMapper;
    private final FileResourceService fileResourceService;
    private final PreviewUrlService previewUrlService;

    @GetMapping("/preview-files/{ticket}")
    public ResponseEntity<Resource> getPreviewFile(@PathVariable String ticket) {
        return signedFileTokenService.verify(ticket)
                .map(this::findActiveFile)
                .map(file -> fileResourceService.asResponse(file, true))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/shares/{token}")
    public Result<PublicFileShareVO> getShare(@PathVariable String token) {
        return fileShareService.resolveActiveShare(token)
                .map(share -> Result.success(share.toPublicView()))
                .orElseGet(() -> Result.error(404, "分享不存在、已撤销或已过期", null));
    }

    @GetMapping("/shares/{token}/download")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token) {
        return fileShareService.resolveActiveShare(token)
                .map(FileShareService.ResolvedShare::fileInfo)
                .map(file -> fileResourceService.asResponse(file, false))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/shares/{token}/preview-ticket")
    public Result<PreviewTicketVO> createSharedPreviewTicket(@PathVariable String token) {
        return fileShareService.resolveActiveShare(token)
                .map(FileShareService.ResolvedShare::fileInfo)
                .map(previewUrlService::createPreviewTicket)
                .map(Result::success)
                .orElseGet(() -> Result.error(404, "分享不存在、已撤销或已过期", null));
    }

    private FileInfo findActiveFile(Long fileId) {
        return fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, fileId)
                .gt(FileInfo::getStatus, 0));
    }
}
