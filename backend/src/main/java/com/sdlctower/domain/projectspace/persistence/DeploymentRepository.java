package com.sdlctower.domain.projectspace.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentRepository extends JpaRepository<DeploymentEntity, String> {

    Optional<DeploymentEntity> findFirstByEnvironmentIdOrderByDeployedAtDesc(String environmentId);
}
