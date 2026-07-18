package com.uniplore.service;

import com.uniplore.config.PreviewProperties;
import com.uniplore.pojo.FileInfo;
import com.uniplore.pojo.PreviewTicketVO;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewUrlServiceTest {

    @Test
    void sourceUrlEndsWithEncodedOriginalFileNameSoKkFileViewCanReadItsSuffix() {
        PreviewProperties properties = new PreviewProperties();
        properties.setSigningSecret("test-signing-secret-with-more-than-32-characters");
        properties.setSourceBaseUrl("http://attachment-service:8001/");
        properties.setPublicBaseUrl("/preview/");
        properties.setTicketTtl(Duration.ofMinutes(2));
        PreviewUrlService service = new PreviewUrlService(properties, new SignedFileTokenService(properties));

        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(42L);
        fileInfo.setFileName("年度 报告+v1.pdf");

        PreviewTicketVO ticket = service.createPreviewTicket(fileInfo);
        String base64SourceUrl = ticket.previewUrl()
                .substring(ticket.previewUrl().indexOf("url=") + 4, ticket.previewUrl().indexOf("&fullfilename="));
        String sourceUrl = new String(Base64.getDecoder().decode(
                URLDecoder.decode(base64SourceUrl, StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

        assertTrue(sourceUrl.startsWith("http://attachment-service:8001/api/public/preview-files/v1."));
        assertTrue(sourceUrl.endsWith("/%E5%B9%B4%E5%BA%A6%20%E6%8A%A5%E5%91%8A%2Bv1.pdf"));
        assertEquals("年度 报告+v1.pdf", URLDecoder.decode(
                sourceUrl.substring(sourceUrl.lastIndexOf('/') + 1), StandardCharsets.UTF_8));
    }
}
