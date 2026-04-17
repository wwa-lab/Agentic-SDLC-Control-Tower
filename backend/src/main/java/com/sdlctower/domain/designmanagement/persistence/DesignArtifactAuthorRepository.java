package com.sdlctower.domain.designmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignArtifactAuthorRepository extends JpaRepository<DesignArtifactAuthorEntity, DesignArtifactAuthorId> {

    List<DesignArtifactAuthorEntity> findByIdArtifactId(String artifactId);

    List<DesignArtifactAuthorEntity> findByIdArtifactIdIn(Collection<String> artifactIds);

    void deleteByIdArtifactId(String artifactId);
}
