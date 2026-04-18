package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.AiDeploymentSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiDeploymentSummaryRepository extends JpaRepository<AiDeploymentSummaryEntity, String> {
    Optional<AiDeploymentSummaryEntity> findTopByDeployIdOrderByGeneratedAtDesc(String deployId);
}
