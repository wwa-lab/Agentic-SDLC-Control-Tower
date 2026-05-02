package com.sdlctower.platform.support;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "SUPPORT_REQUEST")
public class SupportRequestEntity {

    @Id
    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "route", length = 512)
    private String route;

    @Column(name = "reporter_staff_id", length = 64)
    private String reporterStaffId;

    @Column(name = "reporter_mode", length = 32)
    private String reporterMode;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "jira_key", length = 64)
    private String jiraKey;

    @Column(name = "jira_url", length = 1024)
    private String jiraUrl;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    protected SupportRequestEntity() {
    }

    public SupportRequestEntity(String requestId, LocalDate requestDate, SupportRequestDto request, String payloadJson, Instant now) {
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.title = request.title();
        this.category = request.category();
        this.description = request.description();
        this.route = request.route();
        this.reporterStaffId = request.reporterStaffId();
        this.reporterMode = request.reporterMode();
        this.status = "pending";
        this.payloadJson = payloadJson;
        this.attemptCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markCreated(String jiraKey, String jiraUrl, Instant now) {
        this.status = "created";
        this.jiraKey = jiraKey;
        this.jiraUrl = jiraUrl;
        this.attemptCount += 1;
        this.lastAttemptAt = now;
        this.nextRetryAt = null;
        this.updatedAt = now;
    }

    public void markPendingRetry(Instant now) {
        this.status = "pending";
        this.attemptCount += 1;
        this.lastAttemptAt = now;
        this.nextRetryAt = now.plusSeconds(300);
        this.updatedAt = now;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }
}
