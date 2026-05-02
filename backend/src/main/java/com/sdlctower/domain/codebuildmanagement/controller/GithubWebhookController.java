package com.sdlctower.domain.codebuildmanagement.controller;

import com.sdlctower.domain.codebuildmanagement.ingestion.GithubWebhookSignatureVerifier;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import com.sdlctower.domain.codebuildmanagement.persistence.IngestionOutboxEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.IngestionOutboxRepository;
import com.sdlctower.shared.ApiConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.WEBHOOKS_BASE)
public class GithubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GithubWebhookController.class);

    private final GithubWebhookSignatureVerifier signatureVerifier;
    private final IngestionOutboxRepository outboxRepository;

    public GithubWebhookController(
            GithubWebhookSignatureVerifier signatureVerifier,
            IngestionOutboxRepository outboxRepository
    ) {
        this.signatureVerifier = signatureVerifier;
        this.outboxRepository = outboxRepository;
    }

    @PostMapping("/github")
    public ResponseEntity<Void> receiveWebhook(HttpServletRequest request) throws IOException {
        String signatureHeader = request.getHeader("X-Hub-Signature-256");
        String eventType = request.getHeader("X-GitHub-Event");
        String deliveryId = request.getHeader("X-GitHub-Delivery");

        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw CodeBuildManagementException.unauthorized(
                    "CB_WEBHOOK_SIGNATURE_INVALID", "Missing X-Hub-Signature-256 header");
        }

        byte[] rawBody = request.getInputStream().readAllBytes();

        signatureVerifier.verify(rawBody, signatureHeader);

        if (deliveryId == null || deliveryId.isBlank()) {
            deliveryId = UUID.randomUUID().toString();
        }

        Optional<IngestionOutboxEntity> existing = outboxRepository.findByDeliveryId(deliveryId);
        if (existing.isPresent()) {
            log.info("Duplicate webhook delivery ignored: {}", deliveryId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        IngestionOutboxEntity outbox = IngestionOutboxEntity.create(
                "outbox-" + UUID.randomUUID().toString().substring(0, 8),
                deliveryId,
                eventType != null ? eventType : "unknown",
                new String(rawBody),
                Instant.now()
        );
        outboxRepository.save(outbox);

        log.info("Webhook queued: deliveryId={}, eventType={}", deliveryId, eventType);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
