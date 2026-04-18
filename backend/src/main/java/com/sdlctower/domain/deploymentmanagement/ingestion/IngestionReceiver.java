package com.sdlctower.domain.deploymentmanagement.ingestion;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeploymentIngestionOutboxEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.DeploymentIngestionOutboxRepository;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.JenkinsInstanceRepository;
import com.sdlctower.domain.deploymentmanagement.policy.DeploymentException;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IngestionReceiver {

    private final JenkinsInstanceRepository instanceRepo;
    private final JenkinsSignatureVerifier signatureVerifier;
    private final DeploymentIngestionOutboxRepository outboxRepo;

    public IngestionReceiver(JenkinsInstanceRepository instanceRepo,
                              JenkinsSignatureVerifier signatureVerifier,
                              DeploymentIngestionOutboxRepository outboxRepo) {
        this.instanceRepo = instanceRepo;
        this.signatureVerifier = signatureVerifier;
        this.outboxRepo = outboxRepo;
    }

    public void receiveWebhook(String jenkinsInstanceId, String signature, byte[] rawBody) {
        var instance = instanceRepo.findById(jenkinsInstanceId)
                .orElseThrow(() -> new DeploymentException("DP_JENKINS_INSTANCE_UNKNOWN",
                        "Unknown Jenkins instance: " + jenkinsInstanceId));

        if (!signatureVerifier.verify(rawBody, signature, instance.getWebhookSecretEncrypted())) {
            throw new DeploymentException("DP_INGEST_SIGNATURE_INVALID",
                    "Webhook signature verification failed");
        }

        String deliveryId = computeDeliveryId(rawBody);
        if (outboxRepo.findByDeliveryId(deliveryId).isPresent()) {
            return;
        }

        var entity = DeploymentIngestionOutboxEntity.create(
                UUID.randomUUID().toString(), deliveryId, jenkinsInstanceId,
                new String(rawBody, StandardCharsets.UTF_8));
        outboxRepo.save(entity);
    }

    private String computeDeliveryId(byte[] body) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(body);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
