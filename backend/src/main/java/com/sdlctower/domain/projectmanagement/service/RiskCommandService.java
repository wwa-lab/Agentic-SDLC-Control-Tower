package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateRiskRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.RiskDto;
import com.sdlctower.domain.projectmanagement.events.PlanChangeLogPublisher;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.policy.TransitionPolicy;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskCommandService {

    private final RiskSignalRepository riskSignalRepository;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final TransitionPolicy transitionPolicy;
    private final ProjectManagementMapper mapper;
    private final PlanChangeLogPublisher changeLogPublisher;

    public RiskCommandService(
            RiskSignalRepository riskSignalRepository,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            RevisionFencingPolicy revisionFencingPolicy,
            TransitionPolicy transitionPolicy,
            ProjectManagementMapper mapper,
            PlanChangeLogPublisher changeLogPublisher
    ) {
        this.riskSignalRepository = riskSignalRepository;
        this.projectSeedCatalog = projectSeedCatalog;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.transitionPolicy = transitionPolicy;
        this.mapper = mapper;
        this.changeLogPublisher = changeLogPublisher;
    }

    @Transactional
    public RiskDto create(String projectId, CreateRiskRequest request) {
        Instant now = Instant.now();
        long newRevision = revisionFencingPolicy.bump(projectId);
        RiskSignalEntity entity = RiskSignalEntity.create(
                "risk-" + UUID.randomUUID(),
                projectSeedCatalog.project(projectId).workspaceId(),
                projectId,
                request.category(),
                request.severity(),
                "PROJECT",
                projectId,
                request.title(),
                request.title(),
                request.linkedIncidentId() == null ? "Open Plan" : "Open Incident",
                request.linkedIncidentId() == null ? "/project-management/plan/" + projectId : "/incidents/" + request.linkedIncidentId(),
                null,
                null,
                now,
                null
        );
        entity.setPmState("IDENTIFIED");
        entity.setOwnerMemberId(request.ownerMemberId());
        entity.setEscalatedIncidentId(request.linkedIncidentId());
        entity.setPlanRevisionAtUpdate(newRevision);
        riskSignalRepository.save(entity);
        RiskDto after = mapper.toRiskDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "CREATE", "RISK", entity.getId(), null, after, null);
        return after;
    }

    @Transactional
    public RiskDto update(String projectId, String riskId, UpdateRiskRequest request) {
        RiskSignalEntity entity = riskSignalRepository.findByProjectIdAndId(projectId, riskId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Risk " + riskId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        RiskDto before = mapper.toRiskDto(entity, currentRevision);
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.severity() != null) {
            entity.setSeverity(request.severity().toUpperCase());
        }
        if (request.category() != null) {
            entity.setCategory(request.category().toUpperCase());
        }
        if (request.ownerMemberId() != null) {
            entity.setOwnerMemberId(request.ownerMemberId());
        }
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        riskSignalRepository.save(entity);
        RiskDto after = mapper.toRiskDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "UPDATE", "RISK", riskId, before, after, null);
        return after;
    }

    @Transactional
    public RiskDto transition(String projectId, String riskId, TransitionRiskRequest request) {
        RiskSignalEntity entity = riskSignalRepository.findByProjectIdAndId(projectId, riskId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Risk " + riskId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        transitionPolicy.risk(mapper.riskState(entity), request.to());
        transitionPolicy.requireMitigationNote(request.to(), request.mitigationNote());
        transitionPolicy.requireResolutionNote(request.to(), request.resolutionNote());
        transitionPolicy.requireIncidentLink(request.to(), request.linkedIncidentId());

        RiskDto before = mapper.toRiskDto(entity, currentRevision);
        String targetState = request.to().toUpperCase();
        entity.setPmState(targetState);
        if (request.mitigationNote() != null) {
            entity.setMitigationNote(request.mitigationNote());
        }
        if (request.resolutionNote() != null) {
            entity.setResolutionNote(request.resolutionNote());
        }
        if (request.linkedIncidentId() != null) {
            entity.setEscalatedIncidentId(request.linkedIncidentId());
        }
        if ("RESOLVED".equals(targetState)) {
            entity.setResolvedAt(Instant.now());
        }
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        riskSignalRepository.save(entity);
        RiskDto after = mapper.toRiskDto(entity, newRevision);
        changeLogPublisher.publish(
                projectId,
                "ESCALATED".equals(targetState) ? "ESCALATE" : "TRANSITION",
                "RISK",
                riskId,
                before,
                after,
                request.mitigationNote() != null ? request.mitigationNote() : request.resolutionNote()
        );
        return after;
    }
}
