package com.sdlctower.domain.aicenter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "skill_execution", indexes = {
        @Index(name = "idx_exec_ws_started", columnList = "workspace_id, started_at"),
        @Index(name = "idx_exec_ws_skill", columnList = "workspace_id, skill_id, started_at"),
        @Index(name = "idx_exec_ws_status", columnList = "workspace_id, status")
})
public class SkillExecutionEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "skill_id", nullable = false)
    private String skillId;

    @Column(name = "skill_key", nullable = false)
    private String skillKey;

    @Column(nullable = false)
    private String status;

    @Column(name = "trigger_source_type", nullable = false)
    private String triggerSourceType;

    @Column(name = "trigger_source_page")
    private String triggerSourcePage;

    @Column(name = "trigger_source_url")
    private String triggerSourceUrl;

    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;

    @Column(name = "triggered_by_type", nullable = false)
    private String triggeredByType;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "input_summary", columnDefinition = "CLOB")
    private String inputSummaryJson;

    @Column(name = "output_summary", columnDefinition = "CLOB")
    private String outputSummaryJson;

    @Column(name = "step_breakdown", columnDefinition = "CLOB")
    private String stepBreakdownJson;

    @Column(name = "policy_trail", columnDefinition = "CLOB")
    private String policyTrailJson;

    @Column(name = "autonomy_level")
    private String autonomyLevel;

    @Column(name = "time_saved_minutes")
    private Integer timeSavedMinutes;

    @Column(name = "audit_record_id")
    private String auditRecordId;

    @Column(name = "outcome_summary")
    private String outcomeSummary;

    protected SkillExecutionEntity() {}

    public static SkillExecutionEntity create(String id, String workspaceId, String skillId,
                                              String skillKey, String status,
                                              String triggerSourceType, String triggerSourcePage,
                                              String triggerSourceUrl, String triggeredBy,
                                              String triggeredByType, Instant startedAt,
                                              Instant endedAt, Long durationMs,
                                              String inputSummaryJson, String outputSummaryJson,
                                              String stepBreakdownJson, String policyTrailJson,
                                              String autonomyLevel, Integer timeSavedMinutes,
                                              String auditRecordId, String outcomeSummary) {
        SkillExecutionEntity e = new SkillExecutionEntity();
        e.id = id;
        e.workspaceId = workspaceId;
        e.skillId = skillId;
        e.skillKey = skillKey;
        e.status = status;
        e.triggerSourceType = triggerSourceType;
        e.triggerSourcePage = triggerSourcePage;
        e.triggerSourceUrl = triggerSourceUrl;
        e.triggeredBy = triggeredBy;
        e.triggeredByType = triggeredByType;
        e.startedAt = startedAt;
        e.endedAt = endedAt;
        e.durationMs = durationMs;
        e.inputSummaryJson = inputSummaryJson;
        e.outputSummaryJson = outputSummaryJson;
        e.stepBreakdownJson = stepBreakdownJson;
        e.policyTrailJson = policyTrailJson;
        e.autonomyLevel = autonomyLevel;
        e.timeSavedMinutes = timeSavedMinutes;
        e.auditRecordId = auditRecordId;
        e.outcomeSummary = outcomeSummary;
        return e;
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getSkillId() { return skillId; }
    public String getSkillKey() { return skillKey; }
    public String getStatus() { return status; }
    public String getTriggerSourceType() { return triggerSourceType; }
    public String getTriggerSourcePage() { return triggerSourcePage; }
    public String getTriggerSourceUrl() { return triggerSourceUrl; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getTriggeredByType() { return triggeredByType; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Long getDurationMs() { return durationMs; }
    public String getInputSummaryJson() { return inputSummaryJson; }
    public String getOutputSummaryJson() { return outputSummaryJson; }
    public String getStepBreakdownJson() { return stepBreakdownJson; }
    public String getPolicyTrailJson() { return policyTrailJson; }
    public String getAutonomyLevel() { return autonomyLevel; }
    public Integer getTimeSavedMinutes() { return timeSavedMinutes; }
    public String getAuditRecordId() { return auditRecordId; }
    public String getOutcomeSummary() { return outcomeSummary; }
}
