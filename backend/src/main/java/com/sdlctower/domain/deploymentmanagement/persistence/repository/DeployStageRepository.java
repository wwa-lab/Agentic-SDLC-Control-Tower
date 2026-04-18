package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeployStageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeployStageRepository extends JpaRepository<DeployStageEntity, String> {
    List<DeployStageEntity> findByDeployIdOrderByStageOrderAsc(String deployId);
}
