package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementConstraintRepository extends JpaRepository<RequirementConstraintEntity, Long> {

    List<RequirementConstraintEntity> findByRequirementIdOrderByDisplayOrderAsc(String requirementId);
}
