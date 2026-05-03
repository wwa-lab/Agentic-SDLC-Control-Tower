package com.sdlctower.domain.deploymentmanagement.controller;

import com.sdlctower.domain.deploymentmanagement.ingestion.IngestionReceiver;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.JenkinsInstanceRepository;
import com.sdlctower.domain.deploymentmanagement.policy.DeploymentException;
import com.sdlctower.platform.workspace.WorkspaceContext;
import com.sdlctower.platform.workspace.WorkspaceContextHolder;
import com.sdlctower.platform.workspace.WorkspaceContextResolver;
import com.sdlctower.platform.workspace.WorkspaceCrossAccessReason;
import com.sdlctower.shared.ApiConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.WEBHOOKS_BASE)
public class JenkinsWebhookController {

    private final IngestionReceiver ingestionReceiver;
    private final JenkinsInstanceRepository instanceRepo;
    private final WorkspaceContextResolver resolver;

    public JenkinsWebhookController(IngestionReceiver ingestionReceiver,
                                    JenkinsInstanceRepository instanceRepo,
                                    WorkspaceContextResolver resolver) {
        this.ingestionReceiver = ingestionReceiver;
        this.instanceRepo = instanceRepo;
        this.resolver = resolver;
    }

    @PostMapping("/jenkins")
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader("X-Jenkins-Instance") String jenkinsInstanceId,
            @RequestHeader("X-Jenkins-Signature") String signature,
            @RequestBody byte[] rawBody) {
        try {
            // Resolve workspace from jenkins_instance table (cross-workspace lookup).
            String workspaceId = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
                    WorkspaceCrossAccessReason.WEBHOOK_JENKINS_INGEST,
                    () -> instanceRepo.findById(jenkinsInstanceId)
                            .map(inst -> inst.getWorkspaceId())
                            .orElse(null)
            );
            if (workspaceId == null) {
                return ResponseEntity.status(404).build();
            }
            WorkspaceContext ctx = resolver.resolveById(workspaceId, false);
            WorkspaceContextHolder.set(ctx);
            try {
                ingestionReceiver.receiveWebhook(jenkinsInstanceId, signature, rawBody);
            } finally {
                WorkspaceContextHolder.clear();
            }
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
