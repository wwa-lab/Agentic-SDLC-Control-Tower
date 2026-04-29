package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementDocumentQualityGateRunRepository extends JpaRepository<RequirementDocumentQualityGateRunEntity, String> {
    Optional<RequirementDocumentQualityGateRunEntity> findTopByDocumentIdOrderByScoredAtDesc(String documentId);
    List<RequirementDocumentQualityGateRunEntity> findByRequirementIdOrderByScoredAtDesc(String requirementId);
}
