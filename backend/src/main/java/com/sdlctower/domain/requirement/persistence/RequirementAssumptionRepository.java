package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAssumptionRepository extends JpaRepository<RequirementAssumptionEntity, Long> {

    List<RequirementAssumptionEntity> findByRequirementIdOrderByDisplayOrderAsc(String requirementId);
}
