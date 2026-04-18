package com.sdlctower.domain.designmanagement.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignAiSummaryRepository extends JpaRepository<DesignAiSummaryEntity, String> {

    Optional<DesignAiSummaryEntity> findTopByArtifactIdAndVersionIdOrderByGeneratedAtDesc(String artifactId, String versionId);

    List<DesignAiSummaryEntity> findByArtifactIdIn(Collection<String> artifactIds);
}
