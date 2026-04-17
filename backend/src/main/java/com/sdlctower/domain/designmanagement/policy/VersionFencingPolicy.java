package com.sdlctower.domain.designmanagement.policy;

import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import org.springframework.stereotype.Component;

@Component
public class VersionFencingPolicy {

    private final DesignArtifactRepository artifactRepository;

    public VersionFencingPolicy(DesignArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    public void check(String artifactId, String clientPrevVersionId) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        if (artifact.getCurrentVersionId() == null || !artifact.getCurrentVersionId().equals(clientPrevVersionId)) {
            throw DesignManagementException.conflict(
                    "DM_STALE_VERSION",
                    "Artifact " + artifactId + " moved to version " + artifact.getCurrentVersionId()
            );
        }
    }
}
