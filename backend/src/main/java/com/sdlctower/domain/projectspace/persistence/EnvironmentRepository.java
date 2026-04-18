package com.sdlctower.domain.projectspace.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<EnvironmentEntity, String> {

    List<EnvironmentEntity> findByProjectIdOrderByLabelAsc(String projectId);
}
