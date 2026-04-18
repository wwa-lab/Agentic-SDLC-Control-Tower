package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jenkins_instance")
public class JenkinsInstanceEntity {

    @Id private String id;
    @Column(name = "workspace_id", nullable = false) private String workspaceId;
    @Column(nullable = false) private String name;
    @Column(name = "base_url", nullable = false) private String baseUrl;
    @Column(name = "webhook_secret_encrypted") private String webhookSecretEncrypted;
    @Column(name = "api_token_encrypted") private String apiTokenEncrypted;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected JenkinsInstanceEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getName() { return name; }
    public String getBaseUrl() { return baseUrl; }
    public String getWebhookSecretEncrypted() { return webhookSecretEncrypted; }
    public Instant getCreatedAt() { return createdAt; }
}
