package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CapacityEditRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.SaveCapacityBatchRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CapacityCellDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanCapacityMatrixDto;
import com.sdlctower.domain.projectmanagement.events.PlanChangeLogPublisher;
import com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationEntity;
import com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationRepository;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.policy.TransitionPolicy;
import com.sdlctower.domain.projectmanagement.projection.CapacityPlanProjection;
import com.sdlctower.domain.projectspace.persistence.MilestoneEntity;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapacityCommandService {

    private final CapacityAllocationRepository capacityAllocationRepository;
    private final MilestoneRepository milestoneRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final TransitionPolicy transitionPolicy;
    private final CapacityPlanProjection capacityPlanProjection;
    private final ProjectManagementMapper mapper;
    private final PlanChangeLogPublisher changeLogPublisher;

    public CapacityCommandService(
            CapacityAllocationRepository capacityAllocationRepository,
            MilestoneRepository milestoneRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            TransitionPolicy transitionPolicy,
            CapacityPlanProjection capacityPlanProjection,
            ProjectManagementMapper mapper,
            PlanChangeLogPublisher changeLogPublisher
    ) {
        this.capacityAllocationRepository = capacityAllocationRepository;
        this.milestoneRepository = milestoneRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.transitionPolicy = transitionPolicy;
        this.capacityPlanProjection = capacityPlanProjection;
        this.mapper = mapper;
        this.changeLogPublisher = changeLogPublisher;
    }

    @Transactional
    public PlanCapacityMatrixDto saveBatch(String projectId, SaveCapacityBatchRequest request) {
        request.edits().forEach(edit -> revisionFencingPolicy.check(projectId, edit.planRevision()));
        List<CapacityAllocationEntity> existing = capacityAllocationRepository.findByProjectIdOrderByWindowStartAscMemberIdAsc(projectId);
        validateRows(existing, request.edits());

        List<CapacityCellDto> before = request.edits().stream()
                .map(edit -> capacityAllocationRepository.findByProjectIdAndMemberIdAndMilestoneId(projectId, edit.memberId(), edit.milestoneId())
                        .map(mapper::toCapacityCellDto)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        long newRevision = revisionFencingPolicy.bump(projectId);
        Instant now = Instant.now();
        Map<String, MilestoneEntity> milestonesById = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId).stream()
                .collect(Collectors.toMap(MilestoneEntity::getId, milestone -> milestone));

        for (CapacityEditRequest edit : request.edits()) {
            MilestoneEntity milestone = milestonesById.get(edit.milestoneId());
            if (milestone == null) {
                throw ProjectManagementException.notFound(
                        "PM_NOT_FOUND",
                        "Milestone " + edit.milestoneId() + " not found for capacity allocation"
                );
            }
            LocalDate windowStart = milestone.getTargetDate().minusDays(14);
            LocalDate windowEnd = milestone.getTargetDate().plusDays(14);
            CapacityAllocationEntity entity = capacityAllocationRepository
                    .findByProjectIdAndMemberIdAndMilestoneId(projectId, edit.memberId(), edit.milestoneId())
                    .orElseGet(() -> CapacityAllocationEntity.create(
                            "cap-" + UUID.randomUUID(),
                            projectId,
                            edit.memberId(),
                            edit.milestoneId(),
                            edit.percent(),
                            edit.justification(),
                            windowStart,
                            windowEnd,
                            newRevision,
                            now
                    ));
            entity.apply(edit.percent(), edit.justification(), windowStart, windowEnd, newRevision, now);
            capacityAllocationRepository.save(entity);
        }

        PlanCapacityMatrixDto after = capacityPlanProjection.load(projectId);
        changeLogPublisher.publish(
                projectId,
                "UPDATE",
                "CAPACITY_ALLOCATION",
                projectId,
                before,
                after,
                request.edits().stream()
                        .map(CapacityEditRequest::justification)
                        .filter(justification -> justification != null && !justification.isBlank())
                        .findFirst()
                        .orElse(null)
        );
        return after;
    }

    private void validateRows(List<CapacityAllocationEntity> existing, List<CapacityEditRequest> edits) {
        Map<String, DraftCell> result = new LinkedHashMap<>();
        existing.forEach(cell -> result.put(key(cell.getMemberId(), cell.getMilestoneId()), new DraftCell(
                cell.getMemberId(),
                cell.getMilestoneId(),
                cell.getAllocationPercent(),
                cell.getJustification()
        )));
        edits.forEach(edit -> result.put(key(edit.memberId(), edit.milestoneId()), new DraftCell(
                edit.memberId(),
                edit.milestoneId(),
                edit.percent(),
                edit.justification()
        )));

        result.values().stream()
                .collect(Collectors.groupingBy(DraftCell::memberId, LinkedHashMap::new, Collectors.toList()))
                .forEach((memberId, cells) -> {
                    int total = cells.stream().mapToInt(DraftCell::percent).sum();
                    String justification = cells.stream()
                            .map(DraftCell::justification)
                            .filter(value -> value != null && !value.isBlank())
                            .findFirst()
                            .orElse(null);
                    transitionPolicy.requireOverallocationJustification(total, justification, memberId);
                });
    }

    private String key(String memberId, String milestoneId) {
        return memberId + "::" + milestoneId;
    }

    private record DraftCell(String memberId, String milestoneId, int percent, String justification) {}
}
