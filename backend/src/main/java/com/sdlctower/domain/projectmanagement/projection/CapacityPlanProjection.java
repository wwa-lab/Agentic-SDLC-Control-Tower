package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CapacityMemberRefDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CapacityMilestoneRefDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanCapacityMatrixDto;
import com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationRepository;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CapacityPlanProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final CapacityAllocationRepository capacityAllocationRepository;
    private final MilestoneRepository milestoneRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final ProjectManagementMapper mapper;

    public CapacityPlanProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CapacityAllocationRepository capacityAllocationRepository,
            MilestoneRepository milestoneRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.capacityAllocationRepository = capacityAllocationRepository;
        this.milestoneRepository = milestoneRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.mapper = mapper;
    }

    public PlanCapacityMatrixDto load(String projectId) {
        var project = projectSeedCatalog.project(projectId);
        var cells = capacityAllocationRepository.findByProjectIdOrderByWindowStartAscMemberIdAsc(projectId);
        var milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId);
        long revision = revisionFencingPolicy.currentRevision(projectId);

        List<CapacityMilestoneRefDto> milestoneRefs = milestones.stream()
                .map(milestone -> new CapacityMilestoneRefDto(milestone.getId(), milestone.getLabel(), milestone.getOrdering()))
                .toList();

        Set<String> memberIds = new LinkedHashSet<>();
        project.roles().stream().map(ProjectSpaceSeedCatalog.RoleSeed::memberId).filter(java.util.Objects::nonNull).forEach(memberIds::add);
        cells.stream().map(com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationEntity::getMemberId).forEach(memberIds::add);

        List<CapacityMemberRefDto> members = memberIds.stream()
                .map(memberId -> {
                    boolean hasBackup = project.roles().stream()
                            .anyMatch(role -> memberId.equals(role.memberId()) && role.backupMemberId() != null);
                    boolean onCall = project.roles().stream()
                            .anyMatch(role -> memberId.equals(role.memberId()) && "ON".equals(role.oncallStatus()));
                    return new CapacityMemberRefDto(
                            memberId,
                            projectSeedCatalog.memberDisplayName(memberId),
                            hasBackup,
                            onCall
                    );
                })
                .toList();

        Map<String, Integer> rowTotals = new LinkedHashMap<>();
        Map<String, Integer> columnTotals = new LinkedHashMap<>();
        cells.forEach(cell -> {
            rowTotals.merge(cell.getMemberId(), cell.getAllocationPercent(), Integer::sum);
            columnTotals.merge(cell.getMilestoneId(), cell.getAllocationPercent(), Integer::sum);
        });

        return new PlanCapacityMatrixDto(
                milestoneRefs,
                members,
                cells.stream().map(mapper::toCapacityCellDto).toList(),
                rowTotals,
                columnTotals,
                ProjectManagementConstants.CAPACITY_WARNING_THRESHOLD,
                revision
        );
    }
}
