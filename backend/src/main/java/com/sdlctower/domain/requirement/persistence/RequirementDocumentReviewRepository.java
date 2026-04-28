package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementDocumentReviewRepository extends JpaRepository<RequirementDocumentReviewEntity, String> {
    List<RequirementDocumentReviewEntity> findByRequirementIdOrderByCreatedAtDesc(String requirementId);
    List<RequirementDocumentReviewEntity> findByDocumentIdOrderByCreatedAtDesc(String documentId);
}
