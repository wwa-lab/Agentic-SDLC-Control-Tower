package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "design_artifact_version")
public class DesignArtifactVersionEntity {

    @Id
    private String id;

    @Column(name = "artifact_id", nullable = false)
    private String artifactId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Lob
    @Column(name = "html_payload", nullable = false)
    private String htmlPayload;

    @Column(name = "html_size_bytes", nullable = false)
    private long htmlSizeBytes;

    @Column(name = "content_sha256", nullable = false)
    private String contentSha256;

    @Lob
    @Column(name = "changelog_note")
    private String changelogNote;

    @Column(name = "created_by_member_id", nullable = false)
    private String createdByMemberId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DesignArtifactVersionEntity() {}

    public static DesignArtifactVersionEntity create(
            String id,
            String artifactId,
            int versionNumber,
            String htmlPayload,
            long htmlSizeBytes,
            String contentSha256,
            String changelogNote,
            String createdByMemberId,
            Instant createdAt
    ) {
        DesignArtifactVersionEntity entity = new DesignArtifactVersionEntity();
        entity.id = id;
        entity.artifactId = artifactId;
        entity.versionNumber = versionNumber;
        entity.htmlPayload = htmlPayload;
        entity.htmlSizeBytes = htmlSizeBytes;
        entity.contentSha256 = contentSha256;
        entity.changelogNote = changelogNote;
        entity.createdByMemberId = createdByMemberId;
        entity.createdAt = createdAt;
        return entity;
    }

    public String getId() { return id; }
    public String getArtifactId() { return artifactId; }
    public int getVersionNumber() { return versionNumber; }
    public String getHtmlPayload() { return htmlPayload; }
    public long getHtmlSizeBytes() { return htmlSizeBytes; }
    public String getContentSha256() { return contentSha256; }
    public String getChangelogNote() { return changelogNote; }
    public String getCreatedByMemberId() { return createdByMemberId; }
    public Instant getCreatedAt() { return createdAt; }
}
