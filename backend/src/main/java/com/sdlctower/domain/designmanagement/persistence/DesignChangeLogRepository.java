package com.sdlctower.domain.designmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignChangeLogRepository extends JpaRepository<DesignChangeLogEntity, String> {

    List<DesignChangeLogEntity> findByArtifactIdOrderByOccurredAtDesc(String artifactId);
}
