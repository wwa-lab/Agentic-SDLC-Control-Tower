package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_release")
public class ReleaseEntity {

    @Id private String id;
    @Column(name = "application_id", nullable = false) private String applicationId;
    @Column(name = "release_version", nullable = false) private String releaseVersion;
    @Column(name = "build_artifact_slice_id") private String buildArtifactSliceId;
    @Column(name = "build_artifact_id") private String buildArtifactId;
    @Column(nullable = false) private String state;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private String createdBy;
    @Column(name = "jenkins_source_url") private String jenkinsSourceUrl;

    protected ReleaseEntity() {}

    public static ReleaseEntity create(String id, String applicationId, String releaseVersion, String state) {
        var e = new ReleaseEntity();
        e.id = id; e.applicationId = applicationId; e.releaseVersion = releaseVersion;
        e.state = state; e.createdAt = Instant.now();
        return e;
    }

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getReleaseVersion() { return releaseVersion; }
    public String getBuildArtifactSliceId() { return buildArtifactSliceId; }
    public String getBuildArtifactId() { return buildArtifactId; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getJenkinsSourceUrl() { return jenkinsSourceUrl; }
}
