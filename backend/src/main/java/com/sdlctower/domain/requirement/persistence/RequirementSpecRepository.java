package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementSpecRepository extends JpaRepository<RequirementSpecEntity, String> {

    List<RequirementSpecEntity> findByRequirementIdOrderByDisplayOrderAsc(String requirementId);
}
