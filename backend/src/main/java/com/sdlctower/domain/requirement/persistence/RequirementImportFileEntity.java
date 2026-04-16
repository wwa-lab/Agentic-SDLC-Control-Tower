package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_import_file")
public class RequirementImportFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_id", nullable = false)
    private String importId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "source_file_name", nullable = false)
    private String sourceFileName;

    @Column(name = "source_kind", nullable = false)
    private String sourceKind;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private boolean supported;

    @Column(name = "provider_document_id")
    private String providerDocumentId;

    @Column(name = "provider_batch_id")
    private String providerBatchId;

    @Column(name = "provider_status", nullable = false)
    private String providerStatus;

    @Column(name = "error_message", columnDefinition = "CLOB")
    private String errorMessage;

    @Column(name = "extracted_preview", columnDefinition = "CLOB")
    private String extractedPreview;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected RequirementImportFileEntity() {}

    public static RequirementImportFileEntity create(
            String importId,
            String displayName,
            String sourceFileName,
            String sourceKind,
            String fileExtension,
            String contentType,
            long fileSize,
            boolean supported,
            String providerStatus,
            String errorMessage,
            Instant createdAt
    ) {
        RequirementImportFileEntity entity = new RequirementImportFileEntity();
        entity.importId = importId;
        entity.displayName = displayName;
        entity.sourceFileName = sourceFileName;
        entity.sourceKind = sourceKind;
        entity.fileExtension = fileExtension;
        entity.contentType = contentType;
        entity.fileSize = fileSize;
        entity.supported = supported;
        entity.providerStatus = providerStatus;
        entity.errorMessage = errorMessage;
        entity.createdAt = createdAt;
        entity.updatedAt = createdAt;
        return entity;
    }

    public void markUploaded(String providerDocumentId, String providerBatchId, String providerStatus, Instant updatedAt) {
        this.providerDocumentId = providerDocumentId;
        this.providerBatchId = providerBatchId;
        this.providerStatus = providerStatus;
        this.updatedAt = updatedAt;
    }

    public void markCompleted(String providerStatus, String extractedPreview, Instant completedAt) {
        this.providerStatus = providerStatus;
        this.extractedPreview = extractedPreview;
        this.updatedAt = completedAt;
        this.completedAt = completedAt;
    }

    public void markFailed(String providerStatus, String errorMessage, Instant updatedAt) {
        this.providerStatus = providerStatus;
        this.errorMessage = errorMessage;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getImportId() { return importId; }
    public String getDisplayName() { return displayName; }
    public String getSourceFileName() { return sourceFileName; }
    public String getSourceKind() { return sourceKind; }
    public String getFileExtension() { return fileExtension; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public boolean isSupported() { return supported; }
    public String getProviderDocumentId() { return providerDocumentId; }
    public String getProviderBatchId() { return providerBatchId; }
    public String getProviderStatus() { return providerStatus; }
    public String getErrorMessage() { return errorMessage; }
    public String getExtractedPreview() { return extractedPreview; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
