package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementArtifactLinkRepository extends JpaRepository<RequirementArtifactLinkEntity, String> {
    List<RequirementArtifactLinkEntity> findByExecutionIdOrderByCreatedAtAsc(String executionId);
    List<RequirementArtifactLinkEntity> findByRequirementIdOrderByCreatedAtDesc(String requirementId);
}
