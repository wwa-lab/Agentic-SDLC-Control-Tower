package com.sdlctower.domain.designmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DesignArtifactAuthorId implements Serializable {

    @Column(name = "artifact_id")
    private String artifactId;

    @Column(name = "member_id")
    private String memberId;

    protected DesignArtifactAuthorId() {}

    public DesignArtifactAuthorId(String artifactId, String memberId) {
        this.artifactId = artifactId;
        this.memberId = memberId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getMemberId() {
        return memberId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DesignArtifactAuthorId that)) {
            return false;
        }
        return Objects.equals(artifactId, that.artifactId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, memberId);
    }
}
