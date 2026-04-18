package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeploymentChangeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentChangeLogRepository extends JpaRepository<DeploymentChangeLogEntity, String> {
}
