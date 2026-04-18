package com.sdlctower.domain.deploymentmanagement.persistence.repository;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.ApprovalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalEventRepository extends JpaRepository<ApprovalEventEntity, String> {
    List<ApprovalEventEntity> findByDeployIdOrderByDecidedAtAsc(String deployId);
}
