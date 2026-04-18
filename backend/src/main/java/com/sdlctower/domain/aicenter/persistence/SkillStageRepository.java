package com.sdlctower.domain.aicenter.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SkillStageRepository extends JpaRepository<SkillStageEntity, String> {

    List<SkillStageEntity> findBySkillId(String skillId);

    List<SkillStageEntity> findBySkillIdIn(List<String> skillIds);

    @Query("SELECT ss.stageKey, COUNT(DISTINCT ss.skillId) " +
           "FROM SkillStageEntity ss " +
           "JOIN SkillEntity s ON s.id = ss.skillId " +
           "WHERE s.workspaceId = :workspaceId AND s.status = 'active' " +
           "GROUP BY ss.stageKey")
    List<Object[]> countActiveSkillsByStage(String workspaceId);
}
