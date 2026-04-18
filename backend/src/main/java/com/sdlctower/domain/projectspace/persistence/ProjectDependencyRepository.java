package com.sdlctower.domain.projectspace.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectDependencyRepository extends JpaRepository<ProjectDependencyEntity, String> {

    List<ProjectDependencyEntity> findBySourceProjectIdAndDirectionOrderByCreatedAtAsc(String sourceProjectId, String direction);

    List<ProjectDependencyEntity> findBySourceProjectIdOrderByCreatedAtAsc(String sourceProjectId);

    List<ProjectDependencyEntity> findByTargetProjectIdOrderByCreatedAtAsc(String targetProjectId);

    Optional<ProjectDependencyEntity> findBySourceProjectIdAndId(String sourceProjectId, String id);
}
