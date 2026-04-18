package com.sdlctower.domain.projectmanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CapacityEditRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.InternalCreateAiSuggestionRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.SaveCapacityBatchRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.DismissAiSuggestionRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionActionResultDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionDto;
import com.sdlctower.domain.projectmanagement.events.PlanChangeLogPublisher;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionEntity;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionRepository;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiSuggestionService {

    private final AiSuggestionRepository repository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final MilestoneCommandService milestoneCommandService;
    private final RiskCommandService riskCommandService;
    private final DependencyCommandService dependencyCommandService;
    private final CapacityCommandService capacityCommandService;
    private final ProjectManagementMapper mapper;
    private final PlanChangeLogPublisher changeLogPublisher;
    private final ObjectMapper objectMapper;

    public AiSuggestionService(
            AiSuggestionRepository repository,
            RevisionFencingPolicy revisionFencingPolicy,
            MilestoneCommandService milestoneCommandService,
            RiskCommandService riskCommandService,
            DependencyCommandService dependencyCommandService,
            CapacityCommandService capacityCommandService,
            ProjectManagementMapper mapper,
            PlanChangeLogPublisher changeLogPublisher,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.milestoneCommandService = milestoneCommandService;
        this.riskCommandService = riskCommandService;
        this.dependencyCommandService = dependencyCommandService;
        this.capacityCommandService = capacityCommandService;
        this.mapper = mapper;
        this.changeLogPublisher = changeLogPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiSuggestionDto createInternal(InternalCreateAiSuggestionRequest request) {
        if (!repository.findByProjectIdAndTargetTypeAndTargetIdAndSuppressUntilAfter(
                request.projectId(),
                request.targetType().toUpperCase(),
                request.targetId(),
                Instant.now()
        ).isEmpty()) {
            throw ProjectManagementException.conflict(
                    "PM_AI_SUGGESTION_SUPPRESSED",
                    "Suggestion creation suppressed for target " + request.targetId()
            );
        }

        Instant now = Instant.now();
        AiSuggestionEntity entity = AiSuggestionEntity.create(
                "sug-" + UUID.randomUUID(),
                request.projectId(),
                request.kind().toUpperCase(),
                request.targetType().toUpperCase(),
                request.targetId(),
                request.payload(),
                request.confidence() == null ? 0.75d : request.confidence(),
                "PENDING",
                request.skillExecutionId(),
                null,
                now,
                now,
                null
        );
        repository.save(entity);
        return mapper.toAiSuggestionDto(entity);
    }

    @Transactional
    public AiSuggestionActionResultDto accept(String projectId, String suggestionId) {
        AiSuggestionEntity entity = repository.findByProjectIdAndId(projectId, suggestionId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "AI suggestion " + suggestionId + " not found"));
        if (!"PENDING".equals(entity.getState())) {
            throw ProjectManagementException.conflict(
                    "PM_AI_SUGGESTION_ALREADY_RESOLVED",
                    "AI suggestion " + suggestionId + " is already " + entity.getState()
            );
        }

        AiSuggestionDto before = mapper.toAiSuggestionDto(entity);
        applySuggestion(projectId, entity);
        entity.accept(Instant.now());
        repository.save(entity);
        AiSuggestionDto after = mapper.toAiSuggestionDto(entity);
        String auditLinkId = changeLogPublisher.publishAi(
                projectId,
                "ACCEPT_AI_SUGGESTION",
                "AI_SUGGESTION",
                suggestionId,
                entity.getSkillExecutionId(),
                before,
                after,
                "AI suggestion accepted"
        );
        return new AiSuggestionActionResultDto(after, auditLinkId);
    }

    @Transactional
    public AiSuggestionActionResultDto dismiss(String projectId, String suggestionId, DismissAiSuggestionRequest request) {
        AiSuggestionEntity entity = repository.findByProjectIdAndId(projectId, suggestionId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "AI suggestion " + suggestionId + " not found"));
        if (!"PENDING".equals(entity.getState())) {
            throw ProjectManagementException.conflict(
                    "PM_AI_SUGGESTION_ALREADY_RESOLVED",
                    "AI suggestion " + suggestionId + " is already " + entity.getState()
            );
        }

        AiSuggestionDto before = mapper.toAiSuggestionDto(entity);
        Instant now = Instant.now();
        entity.dismiss(now.plus(24, ChronoUnit.HOURS), now);
        repository.save(entity);
        AiSuggestionDto after = mapper.toAiSuggestionDto(entity);
        String auditLinkId = changeLogPublisher.publish(
                projectId,
                "DISMISS_AI_SUGGESTION",
                "AI_SUGGESTION",
                suggestionId,
                before,
                after,
                request == null ? null : request.reason()
        );
        return new AiSuggestionActionResultDto(after, auditLinkId);
    }

    private void applySuggestion(String projectId, AiSuggestionEntity entity) {
        JsonNode payload = parsePayload(entity.getPayloadJson());
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        switch (entity.getTargetType()) {
            case "MILESTONE" -> milestoneCommandService.transition(
                    projectId,
                    entity.getTargetId(),
                    new TransitionMilestoneRequest(
                            "AT_RISK",
                            minimumLength(payload.path("summary").asText("AI suggested slippage update"), 10),
                            null,
                            currentRevision
                    )
            );
            case "RISK" -> riskCommandService.transition(
                    projectId,
                    entity.getTargetId(),
                    new TransitionRiskRequest(
                            "MITIGATING",
                            minimumLength(payload.path("summary").asText("AI suggested mitigation plan"), 20),
                            null,
                            null,
                            currentRevision
                    )
            );
            case "DEPENDENCY" -> dependencyCommandService.transition(
                    projectId,
                    entity.getTargetId(),
                    new TransitionDependencyRequest(
                            "NEGOTIATING",
                            null,
                            null,
                            currentRevision
                    )
            );
            case "CAPACITY_ALLOCATION" -> {
                String memberId = payload.path("memberId").asText(null);
                String milestoneId = payload.path("milestoneId").asText(null);
                int percent = payload.path("percent").asInt(-1);
                if (memberId == null || milestoneId == null || percent < 0) {
                    throw ProjectManagementException.invalid(
                            "PM_VALIDATION_ERROR",
                            "Capacity rebalance suggestion payload is missing memberId, milestoneId, or percent"
                    );
                }
                capacityCommandService.saveBatch(
                        projectId,
                        new SaveCapacityBatchRequest(List.of(new CapacityEditRequest(
                                memberId,
                                milestoneId,
                                percent,
                                payload.path("justification").asText(minimumLength(payload.path("summary").asText("AI rebalance"), 10)),
                                currentRevision
                        )))
                );
            }
            default -> throw ProjectManagementException.invalid(
                    "PM_VALIDATION_ERROR",
                    "Unsupported AI suggestion target type: " + entity.getTargetType()
            );
        }
    }

    private JsonNode parsePayload(String payloadJson) {
        try {
            return objectMapper.readTree(payloadJson);
        } catch (Exception ex) {
            throw ProjectManagementException.invalid("PM_VALIDATION_ERROR", "Invalid AI suggestion payload");
        }
    }

    private String minimumLength(String value, int minLength) {
        if (value != null && value.trim().length() >= minLength) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value == null ? "" : value.trim());
        while (builder.length() < minLength) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append("context");
        }
        return builder.toString();
    }
}
