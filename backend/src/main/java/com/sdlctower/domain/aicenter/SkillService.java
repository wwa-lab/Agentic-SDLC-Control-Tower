package com.sdlctower.domain.aicenter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.aicenter.dto.AggregateMetricsDto;
import com.sdlctower.domain.aicenter.dto.PolicyDto;
import com.sdlctower.domain.aicenter.dto.RunDto;
import com.sdlctower.domain.aicenter.dto.SkillDetailDto;
import com.sdlctower.domain.aicenter.dto.SkillDto;
import com.sdlctower.domain.aicenter.persistence.PolicyEntity;
import com.sdlctower.domain.aicenter.persistence.PolicyRepository;
import com.sdlctower.domain.aicenter.persistence.SkillEntity;
import com.sdlctower.domain.aicenter.persistence.SkillExecutionEntity;
import com.sdlctower.domain.aicenter.persistence.SkillExecutionRepository;
import com.sdlctower.domain.aicenter.persistence.SkillRepository;
import com.sdlctower.domain.aicenter.persistence.SkillStageEntity;
import com.sdlctower.domain.aicenter.persistence.SkillStageRepository;
import com.sdlctower.domain.aicenter.policy.SkillNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);
    private static final Duration WINDOW_30D = Duration.ofDays(30);

    private final SkillRepository skillRepository;
    private final SkillStageRepository skillStageRepository;
    private final PolicyRepository policyRepository;
    private final SkillExecutionRepository executionRepository;
    private final ObjectMapper objectMapper;

    public SkillService(SkillRepository skillRepository,
                        SkillStageRepository skillStageRepository,
                        PolicyRepository policyRepository,
                        SkillExecutionRepository executionRepository,
                        ObjectMapper objectMapper) {
        this.skillRepository = skillRepository;
        this.skillStageRepository = skillStageRepository;
        this.policyRepository = policyRepository;
        this.executionRepository = executionRepository;
        this.objectMapper = objectMapper;
    }

    public List<SkillDto> list(String workspaceId) {
        List<SkillEntity> skills = skillRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        if (skills.isEmpty()) {
            return List.of();
        }

        List<String> skillIds = skills.stream().map(SkillEntity::getId).toList();
        Map<String, List<String>> stagesBySkill = skillStageRepository.findBySkillIdIn(skillIds)
                .stream()
                .collect(Collectors.groupingBy(
                        SkillStageEntity::getSkillId,
                        Collectors.mapping(SkillStageEntity::getStageKey, Collectors.toList())
                ));

        Map<String, Instant> lastExecutedMap = executionRepository
                .findLastExecutedAtByWorkspace(workspaceId)
                .stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Instant) r[1]));

        Instant since30d = Instant.now().minus(WINDOW_30D);

        return skills.stream()
                .map(s -> toSkillDto(s, stagesBySkill, lastExecutedMap, since30d))
                .toList();
    }

    public SkillDetailDto detail(String workspaceId, String skillKey) {
        SkillEntity skill = skillRepository.findByWorkspaceIdAndKey(workspaceId, skillKey)
                .orElseThrow(() -> new SkillNotFoundException(skillKey));

        List<String> stages = skillStageRepository.findBySkillId(skill.getId()).stream()
                .map(SkillStageEntity::getStageKey)
                .toList();

        Map<String, Instant> lastExecutedMap = executionRepository
                .findLastExecutedAtByWorkspace(workspaceId)
                .stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Instant) r[1]));

        Instant since30d = Instant.now().minus(WINDOW_30D);
        Double successRate30d = computeSuccessRate(workspaceId, skill.getId(), since30d);

        SkillDto skillDto = new SkillDto(
                skill.getId(), skill.getKey(), skill.getName(), skill.getCategory(),
                skill.getSubCategory(), skill.getStatus(), skill.getDefaultAutonomy(),
                skill.getOwner(), skill.getDescription(), stages,
                lastExecutedMap.get(skill.getId()), successRate30d, skill.getVersion()
        );

        PolicyDto policyDto = policyRepository.findByWorkspaceIdAndSkillId(workspaceId, skill.getId())
                .map(this::toPolicyDto)
                .orElse(null);

        List<RunDto> recentRuns = executionRepository
                .findTop10ByWorkspaceIdAndSkillIdOrderByStartedAtDesc(workspaceId, skill.getId())
                .stream()
                .map(e -> toRunDto(e, skill.getName()))
                .toList();

        long totalRuns30d = executionRepository.countBySkillSince(workspaceId, skill.getId(), since30d);
        long avgDuration = executionRepository.avgDurationMsBySkillSince(workspaceId, skill.getId(), since30d);
        String adoptionTrend = "flat";

        AggregateMetricsDto aggregateMetrics = new AggregateMetricsDto(
                successRate30d != null ? successRate30d : 0.0,
                avgDuration, adoptionTrend, totalRuns30d
        );

        return new SkillDetailDto(
                skillDto, skill.getInputContract(), skill.getOutputContract(),
                policyDto, recentRuns, aggregateMetrics
        );
    }

    public SkillDto findByKey(String workspaceId, String key) {
        SkillEntity skill = skillRepository.findByWorkspaceIdAndKey(workspaceId, key)
                .orElseThrow(() -> new SkillNotFoundException(key));

        List<String> stages = skillStageRepository.findBySkillId(skill.getId()).stream()
                .map(SkillStageEntity::getStageKey)
                .toList();

        return new SkillDto(
                skill.getId(), skill.getKey(), skill.getName(), skill.getCategory(),
                skill.getSubCategory(), skill.getStatus(), skill.getDefaultAutonomy(),
                skill.getOwner(), skill.getDescription(), stages, null, null, skill.getVersion()
        );
    }

    private SkillDto toSkillDto(SkillEntity s, Map<String, List<String>> stagesBySkill,
                                Map<String, Instant> lastExecutedMap, Instant since30d) {
        List<String> stages = stagesBySkill.getOrDefault(s.getId(), List.of());
        Instant lastExecutedAt = lastExecutedMap.get(s.getId());
        Double successRate = computeSuccessRate(s.getWorkspaceId(), s.getId(), since30d);

        return new SkillDto(
                s.getId(), s.getKey(), s.getName(), s.getCategory(), s.getSubCategory(),
                s.getStatus(), s.getDefaultAutonomy(), s.getOwner(), s.getDescription(),
                stages, lastExecutedAt, successRate, s.getVersion()
        );
    }

    private Double computeSuccessRate(String workspaceId, String skillId, Instant since) {
        long total = executionRepository.countBySkillSince(workspaceId, skillId, since);
        if (total == 0) return null;
        long succeeded = executionRepository.countSucceededBySkillSince(workspaceId, skillId, since);
        return Math.round(1000.0 * succeeded / total) / 1000.0;
    }

    private PolicyDto toPolicyDto(PolicyEntity p) {
        return new PolicyDto(
                null, // skillKey resolved at call site if needed
                p.getAutonomyLevel(),
                parseJsonList(p.getApprovalRequiredActionsJson()),
                parseJsonList(p.getAuthorizedApproverRolesJson()),
                parseJsonMap(p.getRiskThresholdsJson()),
                p.getLastChangedAt(),
                p.getLastChangedBy()
        );
    }

    RunDto toRunDto(SkillExecutionEntity e, String skillName) {
        return new RunDto(
                e.getId(), e.getSkillKey(), skillName, e.getStatus(),
                e.getTriggerSourceType(), e.getTriggerSourcePage(), e.getTriggerSourceUrl(),
                e.getTriggeredBy(), e.getTriggeredByType(),
                e.getStartedAt(), e.getEndedAt(), e.getDurationMs(),
                e.getOutcomeSummary(), e.getAuditRecordId()
        );
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
