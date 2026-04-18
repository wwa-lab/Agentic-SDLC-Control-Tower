package com.sdlctower.domain.codebuildmanagement.ingestion;

import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementWebhookSignatureVerifier")
public class GithubWebhookSignatureVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "sha256=";

    private final byte[] secretBytes;

    public GithubWebhookSignatureVerifier(
            @Value("${sdlctower.code-build.webhook-secret:default-dev-secret}") String webhookSecret
    ) {
        this.secretBytes = webhookSecret.getBytes(StandardCharsets.UTF_8);
    }

    public void verify(byte[] rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            throw CodeBuildManagementException.unauthorized(
                    "CB_WEBHOOK_SIGNATURE_INVALID", "Invalid signature header format");
        }

        String expectedHex = signatureHeader.substring(SIGNATURE_PREFIX.length());
        String actualHex = computeHmac(rawBody);

        if (!MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                actualHex.getBytes(StandardCharsets.UTF_8))) {
            throw CodeBuildManagementException.unauthorized(
                    "CB_WEBHOOK_SIGNATURE_INVALID", "Webhook signature verification failed");
        }
    }

    private String computeHmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretBytes, HMAC_SHA256));
            byte[] hash = mac.doFinal(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 computation failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
