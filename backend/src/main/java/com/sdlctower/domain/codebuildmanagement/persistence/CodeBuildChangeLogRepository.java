package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeBuildChangeLogRepository extends JpaRepository<CodeBuildChangeLogEntity, String> {

    List<CodeBuildChangeLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, String entityId);
}
