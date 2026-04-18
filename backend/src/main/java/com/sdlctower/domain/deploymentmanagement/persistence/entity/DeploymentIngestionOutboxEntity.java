package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_ingestion_outbox")
public class DeploymentIngestionOutboxEntity {

    @Id private String id;
    @Column(name = "delivery_id", nullable = false) private String deliveryId;
    @Column(name = "jenkins_instance_id") private String jenkinsInstanceId;
    @Column(name = "event_type") private String eventType;
    @Column(nullable = false) private String status;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "raw_body") private String rawBody;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;
    @Column(name = "processed_at") private Instant processedAt;
    @Column(name = "error_message", length = 1024) private String errorMessage;

    protected DeploymentIngestionOutboxEntity() {}

    public static DeploymentIngestionOutboxEntity create(String id, String deliveryId,
                                                          String jenkinsInstanceId, String rawBody) {
        var e = new DeploymentIngestionOutboxEntity();
        e.id = id; e.deliveryId = deliveryId; e.jenkinsInstanceId = jenkinsInstanceId;
        e.rawBody = rawBody; e.status = "PENDING"; e.receivedAt = Instant.now();
        return e;
    }

    public String getId() { return id; }
    public String getDeliveryId() { return deliveryId; }
    public String getJenkinsInstanceId() { return jenkinsInstanceId; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getRawBody() { return rawBody; }
    public void setProcessedAt(Instant v) { this.processedAt = v; }
    public void setErrorMessage(String v) { this.errorMessage = v; }
    public Instant getReceivedAt() { return receivedAt; }
}
