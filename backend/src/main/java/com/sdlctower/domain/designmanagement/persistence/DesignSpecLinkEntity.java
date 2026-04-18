package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "design_spec_link")
public class DesignSpecLinkEntity {

    @Id
    private String id;

    @Column(name = "artifact_id", nullable = false)
    private String artifactId;

    @Column(name = "spec_id", nullable = false)
    private String specId;

    @Column(name = "covers_revision", nullable = false)
    private int coversRevision;

    @Column(name = "declared_coverage", nullable = false)
    private String declaredCoverage;

    @Column(name = "linked_by_member_id", nullable = false)
    private String linkedByMemberId;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    protected DesignSpecLinkEntity() {}

    public static DesignSpecLinkEntity create(
            String id,
            String artifactId,
            String specId,
            int coversRevision,
            String declaredCoverage,
            String linkedByMemberId,
            Instant linkedAt
    ) {
        DesignSpecLinkEntity entity = new DesignSpecLinkEntity();
        entity.id = id;
        entity.artifactId = artifactId;
        entity.specId = specId;
        entity.coversRevision = coversRevision;
        entity.declaredCoverage = declaredCoverage;
        entity.linkedByMemberId = linkedByMemberId;
        entity.linkedAt = linkedAt;
        return entity;
    }

    public String getId() { return id; }
    public String getArtifactId() { return artifactId; }
    public String getSpecId() { return specId; }
    public int getCoversRevision() { return coversRevision; }
    public String getDeclaredCoverage() { return declaredCoverage; }
    public String getLinkedByMemberId() { return linkedByMemberId; }
    public Instant getLinkedAt() { return linkedAt; }

    public void setCoversRevision(int coversRevision) {
        this.coversRevision = coversRevision;
    }

    public void setDeclaredCoverage(String declaredCoverage) {
        this.declaredCoverage = declaredCoverage;
    }

    public void setLinkedByMemberId(String linkedByMemberId) {
        this.linkedByMemberId = linkedByMemberId;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }
}
