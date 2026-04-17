package com.sdlctower.domain.projectmanagement.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementEnums.PlanActorType;
import com.sdlctower.domain.projectmanagement.persistence.PlanChangeLogEntryEntity;
import com.sdlctower.domain.projectmanagement.persistence.PlanChangeLogEntryRepository;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PlanChangeLogPublisher {

    private final PlanChangeLogEntryRepository repository;
    private final ProjectManagementActorResolver actorResolver;
    private final ObjectMapper objectMapper;

    public PlanChangeLogPublisher(
            PlanChangeLogEntryRepository repository,
            ProjectManagementActorResolver actorResolver,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.actorResolver = actorResolver;
        this.objectMapper = objectMapper;
    }

    public String publish(
            String projectId,
            String action,
            String targetType,
            String targetId,
            Object before,
            Object after,
            String reason
    ) {
        ProjectManagementActorResolver.Actor actor = actorResolver.currentActor();
        Instant now = Instant.now();
        String auditLinkId = "audit-" + UUID.randomUUID();
        repository.save(PlanChangeLogEntryEntity.create(
                "log-" + UUID.randomUUID(),
                projectId,
                PlanActorType.HUMAN.name(),
                actor.memberId(),
                null,
                action,
                targetType,
                targetId,
                toJson(before),
                toJson(after),
                reason,
                "corr-" + UUID.randomUUID(),
                auditLinkId,
                now
        ));
        return auditLinkId;
    }

    public String publishAi(
            String projectId,
            String action,
            String targetType,
            String targetId,
            String skillExecutionId,
            Object before,
            Object after,
            String reason
    ) {
        Instant now = Instant.now();
        String auditLinkId = "audit-" + UUID.randomUUID();
        repository.save(PlanChangeLogEntryEntity.create(
                "log-" + UUID.randomUUID(),
                projectId,
                PlanActorType.AI.name(),
                null,
                skillExecutionId,
                action,
                targetType,
                targetId,
                toJson(before),
                toJson(after),
                reason,
                "corr-" + UUID.randomUUID(),
                auditLinkId,
                now
        ));
        return auditLinkId;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }
}
