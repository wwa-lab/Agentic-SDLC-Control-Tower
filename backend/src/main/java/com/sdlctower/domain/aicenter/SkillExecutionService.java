package com.sdlctower.domain.aicenter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.aicenter.dto.EvidenceLinkDto;
import com.sdlctower.domain.aicenter.dto.PageDto;
import com.sdlctower.domain.aicenter.dto.PolicyTrailEntryDto;
import com.sdlctower.domain.aicenter.dto.RunDetailDto;
import com.sdlctower.domain.aicenter.dto.RunDto;
import com.sdlctower.domain.aicenter.dto.RunFilter;
import com.sdlctower.domain.aicenter.dto.RunStepDto;
import com.sdlctower.domain.aicenter.persistence.EvidenceLinkEntity;
import com.sdlctower.domain.aicenter.persistence.EvidenceLinkRepository;
import com.sdlctower.domain.aicenter.persistence.SkillEntity;
import com.sdlctower.domain.aicenter.persistence.SkillExecutionEntity;
import com.sdlctower.domain.aicenter.persistence.SkillExecutionRepository;
import com.sdlctower.domain.aicenter.persistence.SkillRepository;
import com.sdlctower.domain.aicenter.policy.SkillExecutionNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SkillExecutionService {

    private static final Logger log = LoggerFactory.getLogger(SkillExecutionService.class);

    private final SkillExecutionRepository executionRepository;
    private final EvidenceLinkRepository evidenceLinkRepository;
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;

    public SkillExecutionService(SkillExecutionRepository executionRepository,
                                 EvidenceLinkRepository evidenceLinkRepository,
                                 SkillRepository skillRepository,
                                 ObjectMapper objectMapper) {
        this.executionRepository = executionRepository;
        this.evidenceLinkRepository = evidenceLinkRepository;
        this.skillRepository = skillRepository;
        this.objectMapper = objectMapper;
    }

    public PageDto<RunDto> page(String workspaceId, RunFilter filter, int page, int size) {
        Specification<SkillExecutionEntity> spec = buildSpecification(workspaceId, filter);
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startedAt"));

        Page<SkillExecutionEntity> result = executionRepository.findAll(spec, pageRequest);

        Map<String, String> skillNames = loadSkillNames(workspaceId);

        List<RunDto> items = result.getContent().stream()
                .map(e -> toRunDto(e, skillNames.getOrDefault(e.getSkillId(), e.getSkillKey())))
                .toList();

        return new PageDto<>(items, page, size, result.getTotalElements(), result.hasNext());
    }

    public RunDetailDto detail(String workspaceId, String executionId) {
        SkillExecutionEntity exec = executionRepository.findByWorkspaceIdAndId(workspaceId, executionId)
                .orElseThrow(() -> new SkillExecutionNotFoundException(executionId));

        String skillName = skillRepository.findById(exec.getSkillId())
                .map(SkillEntity::getName)
                .orElse(exec.getSkillKey());

        RunDto runDto = toRunDto(exec, skillName);

        List<EvidenceLinkDto> evidenceLinks = evidenceLinkRepository
                .findByExecutionIdOrderByPositionAsc(executionId)
                .stream()
                .map(this::toEvidenceLinkDto)
                .toList();

        return new RunDetailDto(
                runDto,
                parseJsonMap(exec.getInputSummaryJson()),
                parseJsonMap(exec.getOutputSummaryJson()),
                parseJsonList(exec.getStepBreakdownJson(), new TypeReference<List<RunStepDto>>() {}),
                parseJsonList(exec.getPolicyTrailJson(), new TypeReference<List<PolicyTrailEntryDto>>() {}),
                evidenceLinks,
                exec.getAutonomyLevel(),
                exec.getTimeSavedMinutes() != null ? exec.getTimeSavedMinutes() : 0
        );
    }

    public void record(SkillExecutionEntity entity) {
        executionRepository.save(entity);
    }

    public List<RunDto> findByTriggerSource(String workspaceId, String sourcePage) {
        Map<String, String> skillNames = loadSkillNames(workspaceId);
        return executionRepository.findByWorkspaceIdAndTriggerSourcePage(workspaceId, sourcePage)
                .stream()
                .map(e -> toRunDto(e, skillNames.getOrDefault(e.getSkillId(), e.getSkillKey())))
                .toList();
    }

    private Specification<SkillExecutionEntity> buildSpecification(String workspaceId, RunFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("workspaceId"), workspaceId));

            if (filter.skillKey() != null && !filter.skillKey().isEmpty()) {
                predicates.add(root.get("skillKey").in(filter.skillKey()));
            }
            if (filter.status() != null && !filter.status().isEmpty()) {
                predicates.add(root.get("status").in(filter.status()));
            }
            if (filter.triggerSourcePage() != null) {
                predicates.add(cb.equal(root.get("triggerSourcePage"), filter.triggerSourcePage()));
            }
            if (filter.startedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), filter.startedAfter()));
            }
            if (filter.startedBefore() != null) {
                predicates.add(cb.lessThan(root.get("startedAt"), filter.startedBefore()));
            }
            if (filter.triggeredByType() != null) {
                predicates.add(cb.equal(root.get("triggeredByType"), filter.triggeredByType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, String> loadSkillNames(String workspaceId) {
        return skillRepository.findByWorkspaceIdOrderByNameAsc(workspaceId).stream()
                .collect(Collectors.toMap(SkillEntity::getId, SkillEntity::getName));
    }

    private RunDto toRunDto(SkillExecutionEntity e, String skillName) {
        return new RunDto(
                e.getId(), e.getSkillKey(), skillName, e.getStatus(),
                e.getTriggerSourceType(), e.getTriggerSourcePage(), e.getTriggerSourceUrl(),
                e.getTriggeredBy(), e.getTriggeredByType(),
                e.getStartedAt(), e.getEndedAt(), e.getDurationMs(),
                e.getOutcomeSummary(), e.getAuditRecordId()
        );
    }

    private EvidenceLinkDto toEvidenceLinkDto(EvidenceLinkEntity e) {
        return new EvidenceLinkDto(e.getTitle(), e.getType(), e.getSourceSystem(), e.getUrl());
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

    private <T> T parseJsonList(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            @SuppressWarnings("unchecked")
            T empty = (T) Collections.emptyList();
            return empty;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", e.getMessage());
            @SuppressWarnings("unchecked")
            T empty = (T) Collections.emptyList();
            return empty;
        }
    }
}
