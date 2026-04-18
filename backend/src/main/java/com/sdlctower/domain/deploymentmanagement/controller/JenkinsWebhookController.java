package com.sdlctower.domain.deploymentmanagement.controller;

import com.sdlctower.domain.deploymentmanagement.ingestion.IngestionReceiver;
import com.sdlctower.domain.deploymentmanagement.policy.DeploymentException;
import com.sdlctower.shared.ApiConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.DEPLOYMENT_MANAGEMENT)
public class JenkinsWebhookController {

    private final IngestionReceiver ingestionReceiver;

    public JenkinsWebhookController(IngestionReceiver ingestionReceiver) {
        this.ingestionReceiver = ingestionReceiver;
    }

    @PostMapping("/webhooks/jenkins")
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader("X-Jenkins-Instance") String jenkinsInstanceId,
            @RequestHeader("X-Jenkins-Signature") String signature,
            @RequestBody byte[] rawBody) {
        try {
            ingestionReceiver.receiveWebhook(jenkinsInstanceId, signature, rawBody);
            return ResponseEntity.accepted().build();
        } catch (DeploymentException e) {
            return switch (e.getErrorCode()) {
                case "DP_INGEST_SIGNATURE_INVALID" -> ResponseEntity.status(401).build();
                case "DP_JENKINS_INSTANCE_UNKNOWN" -> ResponseEntity.status(404).build();
                default -> ResponseEntity.badRequest().build();
            };
        }
    }
}
