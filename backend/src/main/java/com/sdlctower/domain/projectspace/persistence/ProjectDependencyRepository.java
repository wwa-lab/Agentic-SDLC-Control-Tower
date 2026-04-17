package com.sdlctower.domain.projectspace.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectDependencyRepository extends JpaRepository<ProjectDependencyEntity, String> {

    List<ProjectDependencyEntity> findBySourceProjectIdAndDirectionOrderByCreatedAtAsc(String sourceProjectId, String direction);
}
