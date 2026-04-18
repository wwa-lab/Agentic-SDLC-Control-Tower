package com.sdlctower.domain.reportcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "report_export")
public class ReportExport {

    @Id
    private String id;

    @Column(name = "report_run_id", nullable = false)
    private String reportRunId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "report_key", nullable = false)
    private String reportKey;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private String status;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "error_message", columnDefinition = "CLOB")
    private String errorMessage;

    @Column
    private Long bytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    protected ReportExport() {}

    public static ReportExport create(
            String id,
            String reportRunId,
            String userId,
            String reportKey,
            String format,
            Instant createdAt,
            Instant expiresAt
    ) {
        ReportExport entity = new ReportExport();
        entity.id = id;
        entity.reportRunId = reportRunId;
        entity.userId = userId;
        entity.reportKey = reportKey;
        entity.format = format;
        entity.status = "queued";
        entity.createdAt = createdAt;
        entity.expiresAt = expiresAt;
        return entity;
    }

    public String getId() { return id; }
    public String getReportRunId() { return reportRunId; }
    public String getUserId() { return userId; }
    public String getReportKey() { return reportKey; }
    public String getFormat() { return format; }
    public String getStatus() { return status; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getStoragePath() { return storagePath; }
    public String getErrorMessage() { return errorMessage; }
    public Long getBytes() { return bytes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadyAt() { return readyAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public void markGenerating() {
        this.status = "generating";
        this.errorMessage = null;
    }

    public void markReady(String downloadUrl, String storagePath, long bytes, Instant readyAt) {
        this.status = "ready";
        this.downloadUrl = downloadUrl;
        this.storagePath = storagePath;
        this.bytes = bytes;
        this.readyAt = readyAt;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = "failed";
        this.errorMessage = errorMessage;
        this.downloadUrl = null;
        this.storagePath = null;
        this.bytes = null;
        this.readyAt = null;
    }

    public void markExpired() {
        this.status = "expired";
        this.downloadUrl = null;
    }
}
