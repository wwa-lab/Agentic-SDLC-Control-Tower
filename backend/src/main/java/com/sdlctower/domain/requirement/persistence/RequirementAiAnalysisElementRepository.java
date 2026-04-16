package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAiAnalysisElementRepository extends JpaRepository<RequirementAiAnalysisElementEntity, Long> {

    List<RequirementAiAnalysisElementEntity> findByAnalysisIdOrderByDisplayOrderAsc(Long analysisId);
}
