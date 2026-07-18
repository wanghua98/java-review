package com.uniplore.service;

import com.uniplore.config.PreviewProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignedFileTokenServiceTest {

    @Test
    void issuedTokenCanBeVerifiedButTamperedTokenCannot() {
        PreviewProperties properties = properties(Duration.ofMinutes(2));
        SignedFileTokenService service = new SignedFileTokenService(properties);
        service.validateConfiguration();

        SignedFileTokenService.IssuedToken issued = service.issue(42L);

        assertEquals(42L, service.verify(issued.token()).orElseThrow());
        assertFalse(service.verify(issued.token() + "x").isPresent());
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        PreviewProperties properties = properties(Duration.ofMillis(1));
        SignedFileTokenService service = new SignedFileTokenService(properties);
        service.validateConfiguration();

        String token = service.issue(7L).token();
        Thread.sleep(5);

        assertTrue(service.verify(token).isEmpty());
    }

    private static PreviewProperties properties(Duration ttl) {
        PreviewProperties properties = new PreviewProperties();
        properties.setSigningSecret("test-signing-secret-with-more-than-32-characters");
        properties.setTicketTtl(ttl);
        return properties;
    }
}
