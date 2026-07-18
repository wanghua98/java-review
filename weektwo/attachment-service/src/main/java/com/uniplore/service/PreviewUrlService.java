package com.uniplore.service;

import com.uniplore.config.PreviewProperties;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.PreviewTicketVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** 构造经过统一 Nginx 入口访问的 kkFileView 地址。 */
@Service
@RequiredArgsConstructor
public class PreviewUrlService {

    private final PreviewProperties properties;
    private final SignedFileTokenService tokenService;

    public PreviewTicketVO createPreviewTicket(FileInfo fileInfo) {
        SignedFileTokenService.IssuedToken issued = tokenService.issue(fileInfo.getId());
        String sourceUrl = trimTrailingSlash(properties.getSourceBaseUrl())
                + "/api/public/preview-files/" + issued.token();
        String encodedSourceUrl = Base64.getEncoder()
                .encodeToString(sourceUrl.getBytes(StandardCharsets.UTF_8));
        String previewUrl = trimTrailingSlash(properties.getPublicBaseUrl())
                + "/onlinePreview?url=" + urlEncode(encodedSourceUrl)
                + "&fullfilename=" + urlEncode(fileInfo.getFileName());
        return new PreviewTicketVO(previewUrl, issued.expiresAt());
    }

    private static String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
