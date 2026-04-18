package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "design_ai_summary")
public class DesignAiSummaryEntity {

    @Id
    private String id;

    @Column(name = "artifact_id", nullable = false)
    private String artifactId;

    @Column(name = "version_id", nullable = false)
    private String versionId;

    @Column(name = "skill_version", nullable = false)
    private String skillVersion;

    @Column(nullable = false)
    private String status;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Lob
    @Column(name = "error_json")
    private String errorJson;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected DesignAiSummaryEntity() {}

    public static DesignAiSummaryEntity create(
            String id,
            String artifactId,
            String versionId,
            String skillVersion,
            String status,
            String payloadJson,
            String errorJson,
            Instant generatedAt
    ) {
        DesignAiSummaryEntity entity = new DesignAiSummaryEntity();
        entity.id = id;
        entity.artifactId = artifactId;
        entity.versionId = versionId;
        entity.skillVersion = skillVersion;
        entity.status = status;
        entity.payloadJson = payloadJson;
        entity.errorJson = errorJson;
        entity.generatedAt = generatedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getArtifactId() { return artifactId; }
    public String getVersionId() { return versionId; }
    public String getSkillVersion() { return skillVersion; }
    public String getStatus() { return status; }
    public String getPayloadJson() { return payloadJson; }
    public String getErrorJson() { return errorJson; }
    public Instant getGeneratedAt() { return generatedAt; }
}
