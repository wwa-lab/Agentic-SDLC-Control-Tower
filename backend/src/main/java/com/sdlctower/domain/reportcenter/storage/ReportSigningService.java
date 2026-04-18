package com.sdlctower.domain.reportcenter.storage;

import com.sdlctower.domain.reportcenter.config.ReportCenterProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class ReportSigningService {

    private final ReportCenterProperties properties;

    public ReportSigningService(ReportCenterProperties properties) {
        this.properties = properties;
    }

    public String sign(String exportId, String actorId, long expiresAtEpochSeconds) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.signingSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = exportId + "|" + actorId + "|" + expiresAtEpochSeconds;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign report export", ex);
        }
    }

    public boolean verify(String exportId, String actorId, String signature, long expiresAtEpochSeconds) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        if (Instant.now().isAfter(Instant.ofEpochSecond(expiresAtEpochSeconds))) {
            return false;
        }
        return sign(exportId, actorId, expiresAtEpochSeconds).equals(signature);
    }
}
