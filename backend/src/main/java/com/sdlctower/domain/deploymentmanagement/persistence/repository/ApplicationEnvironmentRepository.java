package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.ApplicationEnvironmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationEnvironmentRepository extends JpaRepository<ApplicationEnvironmentEntity, String> {
    List<ApplicationEnvironmentEntity> findByApplicationId(String applicationId);
    Optional<ApplicationEnvironmentEntity> findByApplicationIdAndName(String applicationId, String name);
}
