package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogRepoTileDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementCatalogSummaryProjection")
public class CatalogSummaryProjection {

    private final CatalogGridProjection catalogGridProjection;

    public CatalogSummaryProjection(CatalogGridProjection catalogGridProjection) {
        this.catalogGridProjection = catalogGridProjection;
    }

    public CatalogSummaryDto load(
            String workspaceId,
            String projectId,
            String buildStatus,
            String visibility,
            String search
    ) {
        List<CatalogSectionDto> sections = catalogGridProjection.load(
                workspaceId, projectId, buildStatus, visibility, search);

        List<CatalogRepoTileDto> allTiles = sections.stream()
                .flatMap(section -> section.repos().stream())
                .toList();

        int totalRepos = allTiles.size();
        int reposWithRecentSuccess = countByLatestStatus(allTiles, RunStatus.SUCCESS);
        int reposWithRecentFailure = countByLatestStatus(allTiles, RunStatus.FAILURE);
        int reposWithRunning = countByLatestStatus(allTiles, RunStatus.IN_PROGRESS);
        int reposWithCancelled = countByLatestStatus(allTiles, RunStatus.CANCELLED);
        int reposWithNeutral = countByLatestStatus(allTiles, RunStatus.NEUTRAL);
        int reposWithNoRecentRuns = (int) allTiles.stream()
                .filter(tile -> tile.latestBuildStatus() == null)
                .count();

        return new CatalogSummaryDto(
                workspaceId,
                totalRepos,
                reposWithRecentSuccess,
                reposWithRecentFailure,
                reposWithRunning,
                reposWithCancelled,
                reposWithNeutral,
                reposWithNoRecentRuns
        );
    }

    private int countByLatestStatus(List<CatalogRepoTileDto> tiles, RunStatus status) {
        return (int) tiles.stream()
                .filter(tile -> tile.latestBuildStatus() == status)
                .count();
    }
}
