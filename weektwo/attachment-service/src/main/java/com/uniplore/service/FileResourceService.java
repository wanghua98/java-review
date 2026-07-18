package com.uniplore.service;

import com.uniplore.pojo.FileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** 安全地将数据库文件记录转换为下载响应。 */
@Service
@RequiredArgsConstructor
public class FileResourceService {

    @Value("${file.upload-path}")
    private String uploadPath;

    public ResponseEntity<Resource> asResponse(FileInfo fileInfo, boolean inline) {
        if (fileInfo == null || fileInfo.getStoragePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Path root = Path.of(uploadPath).toAbsolutePath().normalize();
        Path file = root.resolve(fileInfo.getStoragePath()).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String disposition = (inline ? "inline" : "attachment")
                + "; filename*=UTF-8''" + encodedName;
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .header("X-Content-Type-Options", "nosniff")
                    .cacheControl(CacheControl.noStore())
                    .contentLength(Files.size(file))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
