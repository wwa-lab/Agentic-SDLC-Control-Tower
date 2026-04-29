package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementSourceReferenceRepository extends JpaRepository<RequirementSourceReferenceEntity, String> {
    List<RequirementSourceReferenceEntity> findByRequirementIdOrderByCreatedAtAsc(String requirementId);
}
