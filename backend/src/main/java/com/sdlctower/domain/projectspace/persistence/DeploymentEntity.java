package com.sdlctower.domain.projectspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "deployments")
public class DeploymentEntity {

    @Id
    private String id;

    @Column(name = "environment_id", nullable = false)
    private String environmentId;

    @Column(name = "version_ref", nullable = false)
    private String versionRef;

    @Column(name = "build_id", nullable = false)
    private String buildId;

    @Column(nullable = false)
    private String health;

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt;

    @Column(name = "commit_distance_from_prod", nullable = false)
    private int commitDistanceFromProd;

    protected DeploymentEntity() {}

    public String getId() { return id; }
    public String getEnvironmentId() { return environmentId; }
    public String getVersionRef() { return versionRef; }
    public String getBuildId() { return buildId; }
    public String getHealth() { return health; }
    public Instant getDeployedAt() { return deployedAt; }
    public int getCommitDistanceFromProd() { return commitDistanceFromProd; }
}
