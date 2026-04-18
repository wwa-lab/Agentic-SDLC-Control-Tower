package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanHeaderMilestoneDto;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlanHeaderProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;
    private final MilestoneRepository milestoneRepository;
    private final RiskSignalRepository riskSignalRepository;
    private final ProjectDependencyRepository dependencyRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final ProjectManagementMapper mapper;

    public PlanHeaderProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog,
            MilestoneRepository milestoneRepository,
            RiskSignalRepository riskSignalRepository,
            ProjectDependencyRepository dependencyRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
        this.milestoneRepository = milestoneRepository;
        this.riskSignalRepository = riskSignalRepository;
        this.dependencyRepository = dependencyRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.mapper = mapper;
    }

    public PlanHeaderDto load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = projectSeedCatalog.project(projectId);
        TeamSpaceSeedCatalog.WorkspaceSeed workspace = teamSpaceSeedCatalog.workspace(project.workspaceId());
        var milestones = milestoneRepository.findByProjectIdOrderByTargetDateAsc(projectId);
        var risks = riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(projectId);
        var dependencies = dependencyRepository.findBySourceProjectIdOrderByCreatedAtAsc(projectId);

        long atRiskMilestones = milestones.stream()
                .map(mapper::milestoneState)
                .filter(state -> "AT_RISK".equals(state))
                .count();
        long slippedMilestones = milestones.stream()
                .map(mapper::milestoneState)
                .filter(state -> "SLIPPED".equals(state))
                .count();
        long blockedDependencies = dependencies.stream()
                .filter(dependency -> dependency.getBlockerReason() != null && !"RESOLVED".equals(mapper.dependencyState(dependency)))
                .count();
        boolean hasCriticalRisk = risks.stream().anyMatch(risk -> "CRITICAL".equals(risk.getSeverity()));
        boolean hasYellowSignal = atRiskMilestones > 0 || blockedDependencies > 0 || risks.stream().anyMatch(risk -> "HIGH".equals(risk.getSeverity()));
        String planHealth = ProjectManagementProjectionSupport.planHealth(
                slippedMilestones > 0,
                hasCriticalRisk,
                dependencies.stream().anyMatch(dependency -> "RED".equals(dependency.getHealth())),
                hasYellowSignal
        );

        List<String> factors = new ArrayList<>();
        if (atRiskMilestones > 0) {
            factors.add(atRiskMilestones + " AT_RISK milestones");
        }
        if (slippedMilestones > 0) {
            factors.add(slippedMilestones + " SLIPPED milestones");
        }
        if (blockedDependencies > 0) {
            factors.add(blockedDependencies + " blocked dependencies");
        }
        if (hasCriticalRisk) {
            factors.add("Critical risk requires attention");
        }
        if (factors.isEmpty()) {
            factors.add("Plan is currently stable");
        }

        var nextMilestone = milestones.stream()
                .filter(milestone -> {
                    String state = mapper.milestoneState(milestone);
                    return !"COMPLETED".equals(state) && !"ARCHIVED".equals(state);
                })
                .findFirst()
                .orElse(milestones.stream().findFirst().orElse(null));

        String pmBackup = project.roles().stream()
                .filter(role -> "PM".equals(role.role()))
                .map(ProjectSpaceSeedCatalog.RoleSeed::backupMemberId)
                .findFirst()
                .orElse(null);

        return new PlanHeaderDto(
                projectId,
                project.name(),
                project.workspaceId(),
                workspace.name(),
                workspace.applicationId(),
                workspace.applicationName(),
                project.lifecycleStage(),
                planHealth,
                factors,
                nextMilestone == null ? null : new PlanHeaderMilestoneDto(nextMilestone.getId(), nextMilestone.getLabel(), nextMilestone.getTargetDate()),
                project.pmMemberId(),
                projectSeedCatalog.memberDisplayName(project.pmMemberId()),
                pmBackup,
                ProjectManagementConstants.DEFAULT_AUTONOMY_LEVEL,
                project.lastUpdatedAt(),
                revisionFencingPolicy.currentRevision(projectId)
        );
    }
}
