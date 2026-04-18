package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ingestion_outbox")
public class IngestionOutboxEntity {

    @Id
    private String id;

    @Column(name = "delivery_id")
    private String deliveryId;

    @Column(name = "event_type")
    private String eventType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "raw_body")
    private String rawBody;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "error_payload")
    private String errorPayload;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected IngestionOutboxEntity() {}

    public static IngestionOutboxEntity create(String id, String deliveryId, String eventType, String rawBody, Instant receivedAt) {
        IngestionOutboxEntity entity = new IngestionOutboxEntity();
        entity.id = id;
        entity.deliveryId = deliveryId;
        entity.eventType = eventType;
        entity.rawBody = rawBody;
        entity.receivedAt = receivedAt;
        entity.status = "PENDING";
        return entity;
    }

    public String getId() { return id; }
    public String getDeliveryId() { return deliveryId; }
    public String getEventType() { return eventType; }
    public String getRawBody() { return rawBody; }
    public Instant getReceivedAt() { return receivedAt; }
    public String getStatus() { return status; }
    public String getErrorPayload() { return errorPayload; }
    public Instant getProcessedAt() { return processedAt; }
}
