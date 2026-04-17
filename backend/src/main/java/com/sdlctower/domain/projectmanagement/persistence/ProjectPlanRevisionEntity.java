package com.sdlctower.domain.projectmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_plan_revisions")
public class ProjectPlanRevisionEntity {

    @Id
    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "current_revision", nullable = false)
    private long currentRevision;

    protected ProjectPlanRevisionEntity() {}

    public static ProjectPlanRevisionEntity create(String projectId, long currentRevision) {
        ProjectPlanRevisionEntity entity = new ProjectPlanRevisionEntity();
        entity.projectId = projectId;
        entity.currentRevision = currentRevision;
        return entity;
    }

    public String getProjectId() {
        return projectId;
    }

    public long getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(long currentRevision) {
        this.currentRevision = currentRevision;
    }
}
