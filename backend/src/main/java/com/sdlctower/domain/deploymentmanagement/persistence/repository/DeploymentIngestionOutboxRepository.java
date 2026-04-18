package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeploymentIngestionOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeploymentIngestionOutboxRepository extends JpaRepository<DeploymentIngestionOutboxEntity, String> {
    Optional<DeploymentIngestionOutboxEntity> findByDeliveryId(String deliveryId);
    List<DeploymentIngestionOutboxEntity> findTop10ByStatusOrderByReceivedAtAsc(String status);
}
