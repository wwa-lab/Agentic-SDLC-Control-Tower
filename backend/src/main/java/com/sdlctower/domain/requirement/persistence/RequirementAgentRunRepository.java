package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAgentRunRepository extends JpaRepository<RequirementAgentRunEntity, String> {
    List<RequirementAgentRunEntity> findByRequirementIdOrderByCreatedAtDesc(String requirementId);
}
