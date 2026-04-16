package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAcceptanceCriterionRepository extends JpaRepository<RequirementAcceptanceCriterionEntity, Long> {

    List<RequirementAcceptanceCriterionEntity> findByRequirementIdOrderByDisplayOrderAsc(String requirementId);
}
