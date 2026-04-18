package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_application")
public class ApplicationEntity {

    @Id private String id;
    @Column(name = "workspace_id", nullable = false) private String workspaceId;
    @Column(name = "project_id", nullable = false) private String projectId;
    @Column(name = "jenkins_instance_id", nullable = false) private String jenkinsInstanceId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String slug;
    @Column(name = "runtime_label") private String runtimeLabel;
    @Column(name = "jenkins_folder_path") private String jenkinsFolderPath;
    @Column(length = 1024) private String description;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected ApplicationEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getProjectId() { return projectId; }
    public String getJenkinsInstanceId() { return jenkinsInstanceId; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getRuntimeLabel() { return runtimeLabel; }
    public String getJenkinsFolderPath() { return jenkinsFolderPath; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
