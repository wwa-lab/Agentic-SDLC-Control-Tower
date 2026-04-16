package com.sdlctower.domain.requirement.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAiAnalysisRepository extends JpaRepository<RequirementAiAnalysisEntity, Long> {

    Optional<RequirementAiAnalysisEntity> findByRequirementId(String requirementId);
}
