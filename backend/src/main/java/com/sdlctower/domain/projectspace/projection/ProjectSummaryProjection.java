package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.HealthFactorDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MilestoneRefDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectCountersDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectSummaryDto;
import com.sdlctower.domain.projectspace.persistence.MilestoneEntity;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectSummaryProjection implements ProjectSpaceProjection<ProjectSummaryDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;
    private final MilestoneRepository milestoneRepository;
    private final RiskSignalRepository riskSignalRepository;

    public ProjectSummaryProjection(
            ProjectSpaceSeedCatalog seedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog,
            MilestoneRepository milestoneRepository,
            RiskSignalRepository riskSignalRepository
    ) {
        this.seedCatalog = seedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
        this.milestoneRepository = milestoneRepository;
        this.riskSignalRepository = riskSignalRepository;
    }

    @Override
    public ProjectSummaryDto load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = seedCatalog.project(projectId);
        TeamSpaceSeedCatalog.WorkspaceSeed workspace = teamSpaceSeedCatalog.workspace(project.workspaceId());
        List<MilestoneEntity> milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId);
        List<RiskSignalEntity> risks = riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(projectId);

        long criticalRiskCount = risks.stream().filter(risk -> "CRITICAL".equalsIgnoreCase(risk.getSeverity())).count();
        long highRiskCount = risks.stream().filter(risk -> "HIGH".equalsIgnoreCase(risk.getSeverity())).count();
        long criticalHighRiskCount = risks.stream()
                .filter(risk -> "CRITICAL".equalsIgnoreCase(risk.getSeverity()) || "HIGH".equalsIgnoreCase(risk.getSeverity()))
                .count();

        MilestoneRefDto activeMilestone = milestones.stream()
                .filter(MilestoneEntity::isCurrent)
                .findFirst()
                .map(milestone -> new MilestoneRefDto(milestone.getId(), milestone.getLabel(), milestone.getTargetDate()))
                .orElse(null);

        return new ProjectSummaryDto(
                project.id(),
                project.name(),
                workspace.id(),
                workspace.name(),
                workspace.applicationId(),
                workspace.applicationName(),
                project.lifecycleStage(),
                computeHealth(project, criticalRiskCount, highRiskCount, milestones),
                buildFactors(project, criticalRiskCount, highRiskCount, milestones),
                seedCatalog.memberRef(project.pmMemberId()),
                seedCatalog.memberRef(project.techLeadMemberId()),
                activeMilestone,
                new ProjectCountersDto(
                        project.activeSpecCount(),
                        project.openIncidentCount(),
                        project.pendingApprovals(),
                        (int) criticalHighRiskCount
                ),
                project.lastUpdatedAt(),
                "/team?workspaceId=" + workspace.id()
        );
    }

    private List<HealthFactorDto> buildFactors(
            ProjectSpaceSeedCatalog.ProjectSeed project,
            long criticalRiskCount,
            long highRiskCount,
            List<MilestoneEntity> milestones
    ) {
        List<HealthFactorDto> factors = new ArrayList<>();

        if (criticalRiskCount > 0) {
            factors.add(new HealthFactorDto(criticalRiskCount + " critical risks are open", "CRIT"));
        }
        if (project.openIncidentCount() > 0) {
            factors.add(new HealthFactorDto(project.openIncidentCount() + " incidents require attention", "CRIT"));
        }
        if (highRiskCount > 0) {
            factors.add(new HealthFactorDto(highRiskCount + " high-severity risks remain active", "WARN"));
        }
        if (milestones.stream().anyMatch(milestone -> "AT_RISK".equalsIgnoreCase(milestone.getStatus()) || "SLIPPED".equalsIgnoreCase(milestone.getStatus()))) {
            factors.add(new HealthFactorDto("Milestone execution needs recovery", "WARN"));
        }
        if (project.pendingApprovals() > 0) {
            factors.add(new HealthFactorDto(project.pendingApprovals() + " approvals are pending", "INFO"));
        }
        if (factors.isEmpty()) {
            factors.add(new HealthFactorDto("Execution posture is stable", "INFO"));
        }
        return factors;
    }

    private String computeHealth(
            ProjectSpaceSeedCatalog.ProjectSeed project,
            long criticalRiskCount,
            long highRiskCount,
            List<MilestoneEntity> milestones
    ) {
        if (project.openIncidentCount() > 1 || criticalRiskCount > 0) {
            return "RED";
        }
        if (highRiskCount > 0 || milestones.stream().anyMatch(milestone -> "AT_RISK".equalsIgnoreCase(milestone.getStatus()) || "SLIPPED".equalsIgnoreCase(milestone.getStatus()))) {
            return "YELLOW";
        }
        if (project.pendingApprovals() > 5) {
            return "YELLOW";
        }
        return "GREEN";
    }
}
