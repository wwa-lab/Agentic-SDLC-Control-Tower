package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_import_task")
public class RequirementImportTaskEntity {

    @Id
    private String id;

    @Column(name = "kb_name", nullable = false)
    private String kbName;

    @Column(name = "dataset_id")
    private String datasetId;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "CLOB")
    private String message;

    @Column(name = "total_number_of_files", nullable = false)
    private int totalNumberOfFiles;

    @Column(name = "number_of_successes", nullable = false)
    private int numberOfSuccesses;

    @Column(name = "number_of_failures", nullable = false)
    private int numberOfFailures;

    @Column(name = "total_size", nullable = false)
    private long totalSize;

    @Column(name = "supported_file_types", columnDefinition = "CLOB")
    private String supportedFileTypes;

    @Column(name = "unsupported_file_types", columnDefinition = "CLOB")
    private String unsupportedFileTypes;

    @Column(name = "draft_payload_json", columnDefinition = "CLOB")
    private String draftPayloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected RequirementImportTaskEntity() {}

    public static RequirementImportTaskEntity create(
            String id,
            String kbName,
            String datasetId,
            String providerName,
            String profileId,
            String status,
            String message,
            int totalNumberOfFiles,
            int numberOfSuccesses,
            int numberOfFailures,
            long totalSize,
            String supportedFileTypes,
            String unsupportedFileTypes,
            Instant createdAt
    ) {
        RequirementImportTaskEntity entity = new RequirementImportTaskEntity();
        entity.id = id;
        entity.kbName = kbName;
        entity.datasetId = datasetId;
        entity.providerName = providerName;
        entity.profileId = profileId;
        entity.status = status;
        entity.message = message;
        entity.totalNumberOfFiles = totalNumberOfFiles;
        entity.numberOfSuccesses = numberOfSuccesses;
        entity.numberOfFailures = numberOfFailures;
        entity.totalSize = totalSize;
        entity.supportedFileTypes = supportedFileTypes;
        entity.unsupportedFileTypes = unsupportedFileTypes;
        entity.createdAt = createdAt;
        entity.updatedAt = createdAt;
        return entity;
    }

    public void updateProgress(String status, String message, int numberOfSuccesses, int numberOfFailures, Instant updatedAt) {
        this.status = status;
        this.message = message;
        this.numberOfSuccesses = numberOfSuccesses;
        this.numberOfFailures = numberOfFailures;
        this.updatedAt = updatedAt;
    }

    public void storeDraft(String draftPayloadJson, String status, String message, Instant completedAt) {
        this.draftPayloadJson = draftPayloadJson;
        this.status = status;
        this.message = message;
        this.updatedAt = completedAt;
        this.completedAt = completedAt;
    }

    public String getId() { return id; }
    public String getKbName() { return kbName; }
    public String getDatasetId() { return datasetId; }
    public String getProviderName() { return providerName; }
    public String getProfileId() { return profileId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getTotalNumberOfFiles() { return totalNumberOfFiles; }
    public int getNumberOfSuccesses() { return numberOfSuccesses; }
    public int getNumberOfFailures() { return numberOfFailures; }
    public long getTotalSize() { return totalSize; }
    public String getSupportedFileTypes() { return supportedFileTypes; }
    public String getUnsupportedFileTypes() { return unsupportedFileTypes; }
    public String getDraftPayloadJson() { return draftPayloadJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
