package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.ResponsibilityBoundaryDto;
import com.sdlctower.domain.teamspace.dto.WorkspaceSummaryDto;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceProjection implements TeamSpaceProjection<WorkspaceSummaryDto> {

    private final TeamSpaceSeedCatalog seedCatalog;
    private final RiskSignalRepository riskSignalRepository;

    public WorkspaceProjection(TeamSpaceSeedCatalog seedCatalog, RiskSignalRepository riskSignalRepository) {
        this.seedCatalog = seedCatalog;
        this.riskSignalRepository = riskSignalRepository;
    }

    @Override
    public WorkspaceSummaryDto load(String workspaceId) {
        TeamSpaceSeedCatalog.WorkspaceSeed workspace = seedCatalog.workspace(workspaceId);
        return new WorkspaceSummaryDto(
                workspaceId,
                workspace.name(),
                workspace.applicationId(),
                workspace.applicationName(),
                workspace.snowGroupId(),
                workspace.snowGroupName(),
                workspace.projectCount(),
                workspace.environmentCount(),
                resolveHealth(workspaceId, workspace.compatibilityMode()),
                workspace.ownerId(),
                workspace.ownerDisplayName(),
                workspace.compatibilityMode(),
                new ResponsibilityBoundaryDto(
                        java.util.List.of(workspace.applicationName()),
                        workspace.snowGroupName() == null ? java.util.List.of() : java.util.List.of(workspace.snowGroupName()),
                        workspace.projectCount()
                )
        );
    }

    private String resolveHealth(String workspaceId, boolean compatibilityMode) {
        var openRisks = riskSignalRepository.findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc(workspaceId);
        if (openRisks.stream().anyMatch(risk -> "CRITICAL".equalsIgnoreCase(risk.getSeverity()))) {
            return "RED";
        }
        if (compatibilityMode || openRisks.stream().anyMatch(risk -> "HIGH".equalsIgnoreCase(risk.getSeverity()))) {
            return "YELLOW";
        }
        return openRisks.isEmpty() ? "GREEN" : "YELLOW";
    }
}
