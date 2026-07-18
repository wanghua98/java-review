package com.uniplore.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uniplore.mapper.FileInfoMapper;
import com.uniplore.pojo.CreateFileShareRequest;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.FileShareVO;
import com.uniplore.pojo.PreviewTicketVO;
import com.uniplore.result.Result;
import com.uniplore.result.ResultMessage;
import com.uniplore.service.FileShareService;
import com.uniplore.service.PreviewUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 登录用户的预览票据和分享管理接口。 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileAccessController {

    private final FileInfoMapper fileInfoMapper;
    private final PreviewUrlService previewUrlService;
    private final FileShareService fileShareService;

    @PostMapping("/preview-ticket/{fileId}")
    public Result<PreviewTicketVO> createPreviewTicket(@PathVariable Long fileId) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        FileInfo fileInfo = findOwnedFile(fileId, StpUtil.getLoginIdAsLong());
        if (fileInfo == null) {
            return Result.error(404, "文件不存在或无权预览", null);
        }
        return Result.success(previewUrlService.createPreviewTicket(fileInfo));
    }

    @PostMapping("/shares")
    public Result<FileShareVO> createShare(@RequestBody CreateFileShareRequest request) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        if (request == null || request.fileId() == null) {
            return Result.error(400, ResultMessage.INVALID_PARAMETERS.getMessage(), null);
        }
        try {
            return Result.success(fileShareService.createShare(
                    request.fileId(), request.expiresInHours(), StpUtil.getLoginIdAsLong()));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }

    @GetMapping("/shares")
    public Result<List<FileShareVO>> listShares() {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        return Result.success(fileShareService.listUserShares(StpUtil.getLoginIdAsLong()));
    }

    @PostMapping("/shares/{shareId}/revoke")
    public Result<String> revokeShare(@PathVariable Long shareId) {
        if (!StpUtil.isLogin()) {
            return Result.error(401, ResultMessage.USER_NOT_LOGGED_IN.getMessage(), null);
        }
        try {
            fileShareService.revokeShare(shareId, StpUtil.getLoginIdAsLong());
            return Result.success("分享已撤销");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage(), null);
        }
    }

    private FileInfo findOwnedFile(Long fileId, Long userId) {
        return fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, fileId)
                .eq(FileInfo::getCreateUser, userId)
                .gt(FileInfo::getStatus, 0));
    }
}
