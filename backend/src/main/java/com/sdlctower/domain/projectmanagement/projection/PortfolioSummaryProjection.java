package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioSummaryDto;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionRepository;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import org.springframework.stereotype.Component;

@Component
public class PortfolioSummaryProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final MilestoneRepository milestoneRepository;
    private final RiskSignalRepository riskSignalRepository;
    private final ProjectDependencyRepository dependencyRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final ProjectManagementMapper mapper;

    public PortfolioSummaryProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            MilestoneRepository milestoneRepository,
            RiskSignalRepository riskSignalRepository,
            ProjectDependencyRepository dependencyRepository,
            AiSuggestionRepository aiSuggestionRepository,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.milestoneRepository = milestoneRepository;
        this.riskSignalRepository = riskSignalRepository;
        this.dependencyRepository = dependencyRepository;
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.mapper = mapper;
    }

    public PortfolioSummaryDto load(String workspaceId) {
        int activeProjects = 0;
        int redProjects = 0;
        int atRiskOrSlippedMilestones = 0;
        int criticalRisks = 0;
        int blockedDependencies = 0;
        int pendingApprovals = 0;
        int aiPendingReview = 0;

        for (ProjectSpaceSeedCatalog.ProjectSeed project : projectSeedCatalog.projectsForWorkspace(workspaceId)) {
            activeProjects++;
            pendingApprovals += project.pendingApprovals();

            var milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(project.id());
            var risks = riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(project.id());
            var dependencies = dependencyRepository.findBySourceProjectIdOrderByCreatedAtAsc(project.id());

            boolean hasSlipped = false;
            boolean hasCriticalRisk = false;
            boolean hasRedDependency = false;
            boolean hasYellowSignal = false;

            for (var milestone : milestones) {
                String state = mapper.milestoneState(milestone);
                if ("AT_RISK".equals(state) || "SLIPPED".equals(state)) {
                    atRiskOrSlippedMilestones++;
                }
                hasSlipped = hasSlipped || "SLIPPED".equals(state);
                hasYellowSignal = hasYellowSignal || "AT_RISK".equals(state) || "IN_PROGRESS".equals(state);
            }

            for (var risk : risks) {
                if ("CRITICAL".equals(risk.getSeverity())) {
                    criticalRisks++;
                    hasCriticalRisk = true;
                }
                hasYellowSignal = hasYellowSignal || "HIGH".equals(risk.getSeverity());
            }

            for (var dependency : dependencies) {
                if (dependency.getBlockerReason() != null && !"RESOLVED".equals(mapper.dependencyState(dependency))) {
                    blockedDependencies++;
                }
                hasRedDependency = hasRedDependency || "RED".equals(dependency.getHealth());
                hasYellowSignal = hasYellowSignal || "YELLOW".equals(dependency.getHealth());
            }

            aiPendingReview += aiSuggestionRepository.findByProjectIdAndStateOrderByCreatedAtDesc(project.id(), "PENDING").size();

            if ("RED".equals(ProjectManagementProjectionSupport.planHealth(hasSlipped, hasCriticalRisk, hasRedDependency, hasYellowSignal))) {
                redProjects++;
            }
        }

        return new PortfolioSummaryDto(
                workspaceId,
                activeProjects,
                redProjects,
                atRiskOrSlippedMilestones,
                criticalRisks,
                blockedDependencies,
                pendingApprovals,
                aiPendingReview,
                ProjectManagementConstants.REFERENCE_NOW
        );
    }
}
