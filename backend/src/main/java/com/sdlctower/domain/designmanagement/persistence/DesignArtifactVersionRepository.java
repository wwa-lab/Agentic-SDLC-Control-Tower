package com.sdlctower.domain.designmanagement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignArtifactVersionRepository extends JpaRepository<DesignArtifactVersionEntity, String> {

    List<DesignArtifactVersionEntity> findByArtifactIdOrderByVersionNumberDesc(String artifactId);

    Optional<DesignArtifactVersionEntity> findByIdAndArtifactId(String id, String artifactId);
}
