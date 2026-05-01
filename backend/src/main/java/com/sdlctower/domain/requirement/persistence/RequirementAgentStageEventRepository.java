package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementAgentStageEventRepository extends JpaRepository<RequirementAgentStageEventEntity, String> {
    List<RequirementAgentStageEventEntity> findByExecutionIdOrderByCreatedAtAsc(String executionId);
}
