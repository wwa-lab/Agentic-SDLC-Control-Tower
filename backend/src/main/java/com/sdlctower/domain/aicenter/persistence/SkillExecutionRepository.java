package com.sdlctower.domain.aicenter.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SkillExecutionRepository
        extends JpaRepository<SkillExecutionEntity, String>,
                JpaSpecificationExecutor<SkillExecutionEntity> {

    Optional<SkillExecutionEntity> findByWorkspaceIdAndId(String workspaceId, String id);

    List<SkillExecutionEntity> findTop10ByWorkspaceIdAndSkillIdOrderByStartedAtDesc(
            String workspaceId, String skillId);

    @Query("SELECT e.skillId, MAX(e.startedAt) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId GROUP BY e.skillId")
    List<Object[]> findLastExecutedAtByWorkspace(String workspaceId);

    @Query("SELECT COUNT(e) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.status = 'succeeded' " +
           "AND e.startedAt >= :since")
    long countSucceededSince(String workspaceId, Instant since);

    @Query("SELECT COUNT(e) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.startedAt >= :since")
    long countAllSince(String workspaceId, Instant since);

    @Query("SELECT COALESCE(SUM(e.timeSavedMinutes), 0) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.startedAt >= :since")
    long sumTimeSavedMinutesSince(String workspaceId, Instant since);

    @Query("SELECT COALESCE(AVG(e.durationMs), 0) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.skillId = :skillId " +
           "AND e.startedAt >= :since")
    long avgDurationMsBySkillSince(String workspaceId, String skillId, Instant since);

    @Query("SELECT COUNT(e) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.skillId = :skillId " +
           "AND e.status = 'succeeded' AND e.startedAt >= :since")
    long countSucceededBySkillSince(String workspaceId, String skillId, Instant since);

    @Query("SELECT COUNT(e) FROM SkillExecutionEntity e " +
           "WHERE e.workspaceId = :workspaceId AND e.skillId = :skillId " +
           "AND e.startedAt >= :since")
    long countBySkillSince(String workspaceId, String skillId, Instant since);

    List<SkillExecutionEntity> findByWorkspaceIdAndTriggerSourcePage(
            String workspaceId, String triggerSourcePage);
}
