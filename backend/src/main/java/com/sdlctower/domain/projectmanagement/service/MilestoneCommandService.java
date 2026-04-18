package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.ArchiveCommandRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateMilestoneRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.MilestoneDto;
import com.sdlctower.domain.projectmanagement.events.PlanChangeLogPublisher;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.policy.TransitionPolicy;
import com.sdlctower.domain.projectspace.persistence.MilestoneEntity;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilestoneCommandService {

    private final MilestoneRepository milestoneRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final TransitionPolicy transitionPolicy;
    private final ProjectManagementMapper mapper;
    private final PlanChangeLogPublisher changeLogPublisher;

    public MilestoneCommandService(
            MilestoneRepository milestoneRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            TransitionPolicy transitionPolicy,
            ProjectManagementMapper mapper,
            PlanChangeLogPublisher changeLogPublisher
    ) {
        this.milestoneRepository = milestoneRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.transitionPolicy = transitionPolicy;
        this.mapper = mapper;
        this.changeLogPublisher = changeLogPublisher;
    }

    @Transactional
    public MilestoneDto create(String projectId, CreateMilestoneRequest request) {
        Instant now = Instant.now();
        long newRevision = revisionFencingPolicy.bump(projectId);
        boolean isCurrent = milestoneRepository.findFirstByProjectIdAndIsCurrentTrueOrderByOrderingAsc(projectId).isEmpty();
        MilestoneEntity entity = MilestoneEntity.create(
                "ms-" + UUID.randomUUID(),
                projectId,
                request.label(),
                request.description(),
                request.targetDate(),
                "NOT_STARTED",
                0,
                request.ownerMemberId(),
                null,
                isCurrent,
                request.ordering(),
                now,
                now
        );
        entity.setBaselineTargetDate(request.targetDate());
        entity.setPlanRevisionAtUpdate(newRevision);
        milestoneRepository.save(entity);
        MilestoneDto after = mapper.toMilestoneDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "CREATE", "MILESTONE", entity.getId(), null, after, null);
        return after;
    }

    @Transactional
    public MilestoneDto update(String projectId, String milestoneId, UpdateMilestoneRequest request) {
        MilestoneEntity entity = milestoneRepository.findByProjectIdAndId(projectId, milestoneId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Milestone " + milestoneId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        MilestoneDto before = mapper.toMilestoneDto(entity, currentRevision);
        if (request.label() != null) {
            entity.setLabel(request.label());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.targetDate() != null) {
            entity.setTargetDate(request.targetDate());
        }
        if (request.ownerMemberId() != null) {
            entity.setOwnerMemberId(request.ownerMemberId());
        }
        if (request.ordering() != null) {
            entity.setOrdering(request.ordering());
        }
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        entity.setUpdatedAt(Instant.now());
        milestoneRepository.save(entity);
        MilestoneDto after = mapper.toMilestoneDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "UPDATE", "MILESTONE", milestoneId, before, after, null);
        return after;
    }

    @Transactional
    public MilestoneDto transition(String projectId, String milestoneId, TransitionMilestoneRequest request) {
        MilestoneEntity entity = milestoneRepository.findByProjectIdAndId(projectId, milestoneId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Milestone " + milestoneId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        transitionPolicy.milestone(mapper.milestoneState(entity), request.to());
        transitionPolicy.requireSlippageReason(request.to(), request.slippageReason());
        transitionPolicy.requireRecoveryTargetDate(request.to(), request.newTargetDate());

        MilestoneDto before = mapper.toMilestoneDto(entity, currentRevision);
        applyTransition(projectId, entity, request.to(), request.slippageReason(), request.newTargetDate());

        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        entity.setUpdatedAt(Instant.now());
        milestoneRepository.save(entity);
        MilestoneDto after = mapper.toMilestoneDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "TRANSITION", "MILESTONE", milestoneId, before, after, request.slippageReason());
        return after;
    }

    @Transactional
    public MilestoneDto archive(String projectId, String milestoneId, ArchiveCommandRequest request) {
        MilestoneEntity entity = milestoneRepository.findByProjectIdAndId(projectId, milestoneId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Milestone " + milestoneId + " not found"));
        if (request != null && request.planRevision() != null) {
            revisionFencingPolicy.check(projectId, request.planRevision());
        }
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        MilestoneDto before = mapper.toMilestoneDto(entity, currentRevision);
        applyTransition(projectId, entity, "ARCHIVED", entity.getSlippageReason(), null);
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        entity.setUpdatedAt(Instant.now());
        milestoneRepository.save(entity);
        MilestoneDto after = mapper.toMilestoneDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "ARCHIVE", "MILESTONE", milestoneId, before, after, null);
        return after;
    }

    private void applyTransition(
            String projectId,
            MilestoneEntity entity,
            String to,
            String slippageReason,
            java.time.LocalDate newTargetDate
    ) {
        Instant now = Instant.now();
        entity.setStatus(to.toUpperCase());
        entity.setPmState(to.toUpperCase());
        if ("AT_RISK".equalsIgnoreCase(to) || "SLIPPED".equalsIgnoreCase(to)) {
            entity.setSlippageReason(slippageReason);
        }
        if ("IN_PROGRESS".equalsIgnoreCase(to)) {
            entity.setArchivedAt(null);
            if (newTargetDate != null) {
                entity.setTargetDate(newTargetDate);
            }
            markCurrent(projectId, entity.getId());
        }
        if ("COMPLETED".equalsIgnoreCase(to)) {
            entity.setCompletedAt(now);
            entity.setPercentComplete(100);
            entity.setCurrent(false);
            promoteNextCurrent(projectId, entity.getId());
        }
        if ("ARCHIVED".equalsIgnoreCase(to)) {
            entity.setArchivedAt(now);
            entity.setCurrent(false);
            promoteNextCurrent(projectId, entity.getId());
        }
    }

    private void markCurrent(String projectId, String milestoneId) {
        List<MilestoneEntity> milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId);
        milestones.forEach(milestone -> milestone.setCurrent(milestone.getId().equals(milestoneId)));
        milestoneRepository.saveAll(milestones);
    }

    private void promoteNextCurrent(String projectId, String excludedMilestoneId) {
        List<MilestoneEntity> milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId);
        boolean promoted = false;
        for (MilestoneEntity milestone : milestones) {
            if (milestone.getId().equals(excludedMilestoneId)) {
                milestone.setCurrent(false);
                continue;
            }
            String state = mapper.milestoneState(milestone);
            if (!promoted && !"COMPLETED".equals(state) && !"ARCHIVED".equals(state)) {
                milestone.setCurrent(true);
                promoted = true;
            } else {
                milestone.setCurrent(false);
            }
        }
        milestoneRepository.saveAll(milestones);
    }
}
