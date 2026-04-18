package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionEntity;
import com.sdlctower.domain.projectmanagement.persistence.CapacityAllocationEntity;
import com.sdlctower.domain.projectmanagement.persistence.PlanChangeLogEntryEntity;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.MilestoneEntity;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagementMapper {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;

    public ProjectManagementMapper(ProjectSpaceSeedCatalog projectSeedCatalog) {
        this.projectSeedCatalog = projectSeedCatalog;
    }

    public ProjectManagementDtos.MilestoneDto toMilestoneDto(MilestoneEntity entity, long revision) {
        ProjectManagementDtos.MilestoneSlippageDto slippage = entity.getSlippageRiskScore() == null
                ? null
                : new ProjectManagementDtos.MilestoneSlippageDto(
                        entity.getSlippageRiskScore(),
                        entity.getSlippageRiskFactors() == null || entity.getSlippageRiskFactors().isBlank()
                                ? List.of()
                                : List.of(new ProjectManagementDtos.MilestoneSlippageFactorDto(entity.getSlippageRiskFactors(), null)),
                        ProjectManagementConstants.REFERENCE_NOW
                );
        return new ProjectManagementDtos.MilestoneDto(
                entity.getId(),
                entity.getProjectId(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getTargetDate(),
                milestoneState(entity),
                entity.getPercentComplete(),
                entity.getOwnerMemberId(),
                entity.getOwnerMemberId() == null ? null : projectSeedCatalog.memberDisplayName(entity.getOwnerMemberId()),
                entity.getSlippageReason(),
                entity.getOrdering(),
                slippage,
                revision,
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getArchivedAt()
        );
    }

    public ProjectManagementDtos.RiskDto toRiskDto(RiskSignalEntity entity, long revision) {
        String ownerId = entity.getOwnerMemberId() != null ? entity.getOwnerMemberId() : projectSeedCatalog.riskOwner(entity.getProjectId(), entity.getCategory()).memberId();
        return new ProjectManagementDtos.RiskDto(
                entity.getId(),
                entity.getProjectId(),
                entity.getTitle(),
                entity.getSeverity(),
                entity.getCategory(),
                riskState(entity),
                ownerId,
                projectSeedCatalog.memberDisplayName(ownerId),
                entity.getMitigationNote(),
                entity.getResolutionNote(),
                entity.getEscalatedIncidentId(),
                Math.toIntExact(Duration.between(entity.getDetectedAt(), ProjectManagementConstants.REFERENCE_NOW).toDays()),
                entity.getDetectedAt(),
                entity.getResolvedAt(),
                revision
        );
    }

    public ProjectManagementDtos.DependencyDto toDependencyDto(ProjectDependencyEntity entity, long revision) {
        return new ProjectManagementDtos.DependencyDto(
                entity.getId(),
                entity.getSourceProjectId(),
                entity.getTargetRef(),
                entity.getTargetProjectId(),
                entity.getTargetName(),
                entity.isExternal(),
                entity.getDirection(),
                entity.getRelationship(),
                entity.getOwnerTeam(),
                entity.getHealth(),
                dependencyState(entity),
                entity.getBlockerReason(),
                entity.getContractCommitment(),
                entity.getRejectionReason(),
                entity.getCounterSignedBy(),
                entity.getCounterSignedAt(),
                Math.max(0, Duration.between(entity.getCreatedAt(), ProjectManagementConstants.REFERENCE_NOW).toDays()),
                revision
        );
    }

    public ProjectManagementDtos.CapacityCellDto toCapacityCellDto(CapacityAllocationEntity entity) {
        return new ProjectManagementDtos.CapacityCellDto(
                entity.getId(),
                entity.getMemberId(),
                entity.getMilestoneId(),
                entity.getAllocationPercent(),
                entity.getJustification(),
                entity.getWindowStart(),
                entity.getWindowEnd(),
                entity.getPlanRevision()
        );
    }

    public ProjectManagementDtos.AiSuggestionDto toAiSuggestionDto(AiSuggestionEntity entity) {
        return new ProjectManagementDtos.AiSuggestionDto(
                entity.getId(),
                entity.getProjectId(),
                entity.getKind(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getPayloadJson(),
                entity.getConfidence(),
                entity.getState(),
                entity.getSkillExecutionId(),
                entity.getSuppressUntil(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }

    public ProjectManagementDtos.ChangeLogEntryDto toChangeLogEntryDto(PlanChangeLogEntryEntity entity) {
        return new ProjectManagementDtos.ChangeLogEntryDto(
                entity.getId(),
                entity.getProjectId(),
                entity.getActorType(),
                entity.getActorMemberId(),
                entity.getActorMemberId() == null ? "AI" : projectSeedCatalog.memberDisplayName(entity.getActorMemberId()),
                entity.getSkillExecutionId(),
                entity.getAction(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getBeforeJson(),
                entity.getAfterJson(),
                entity.getReason(),
                entity.getCorrelationId(),
                entity.getAuditLinkId(),
                entity.getCreatedAt()
        );
    }

    public String milestoneState(MilestoneEntity entity) {
        return entity.getPmState() == null || entity.getPmState().isBlank() ? entity.getStatus() : entity.getPmState();
    }

    public String riskState(RiskSignalEntity entity) {
        return entity.getPmState() == null || entity.getPmState().isBlank() ? "IDENTIFIED" : entity.getPmState();
    }

    public String dependencyState(ProjectDependencyEntity entity) {
        return entity.getPmState() == null || entity.getPmState().isBlank() ? "PROPOSED" : entity.getPmState();
    }
}
