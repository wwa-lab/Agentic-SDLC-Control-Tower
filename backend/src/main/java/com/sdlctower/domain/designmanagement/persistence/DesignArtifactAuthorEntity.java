package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "design_artifact_author")
public class DesignArtifactAuthorEntity {

    @EmbeddedId
    private DesignArtifactAuthorId id;

    protected DesignArtifactAuthorEntity() {}

    public static DesignArtifactAuthorEntity create(String artifactId, String memberId) {
        DesignArtifactAuthorEntity entity = new DesignArtifactAuthorEntity();
        entity.id = new DesignArtifactAuthorId(artifactId, memberId);
        return entity;
    }

    public DesignArtifactAuthorId getId() {
        return id;
    }
}
