package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogRepoTileDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RepoVisibility;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementCatalogGridProjection")
public class CatalogGridProjection {

    private final RepoRepository repoRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public CatalogGridProjection(
            RepoRepository repoRepository,
            PipelineRunRepository pipelineRunRepository,
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog
    ) {
        this.repoRepository = repoRepository;
        this.pipelineRunRepository = pipelineRunRepository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    public List<CatalogSectionDto> load(
            String workspaceId,
            String projectId,
            String buildStatus,
            String visibility,
            String search
    ) {
        List<RepoEntity> allRepos = repoRepository.findAll().stream()
                .filter(repo -> workspaceId.equals(repo.getWorkspaceId()))
                .filter(repo -> projectId == null || projectId.isBlank() || projectId.equals(repo.getProjectId()))
                .filter(repo -> matchesVisibility(repo, visibility))
                .toList();

        List<CatalogRepoTileDto> tiles = allRepos.stream()
                .map(this::toTile)
                .filter(tile -> matchesBuildStatus(tile, buildStatus))
                .filter(tile -> matchesSearch(tile, search))
                .toList();

        Map<String, List<CatalogRepoTileDto>> grouped = new LinkedHashMap<>();
        for (CatalogRepoTileDto tile : tiles) {
            grouped.computeIfAbsent(tile.projectId(), key -> new java.util.ArrayList<>()).add(tile);
        }

        return grouped.entrySet().stream()
                .map(entry -> new CatalogSectionDto(
                        entry.getKey(),
                        resolveProjectName(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    public CatalogFiltersDto loadFilters(String workspaceId) {
        List<RepoEntity> repos = repoRepository.findAll().stream()
                .filter(repo -> workspaceId.equals(repo.getWorkspaceId()))
                .toList();

        List<String> projectIds = repos.stream()
                .map(RepoEntity::getProjectId)
                .distinct()
                .toList();

        return new CatalogFiltersDto(
                projectIds,
                Arrays.asList(RunStatus.values()),
                Arrays.asList(RepoVisibility.values())
        );
    }

    private CatalogRepoTileDto toTile(RepoEntity repo) {
        List<PipelineRunEntity> recentRuns = pipelineRunRepository
                .findTop25ByRepoIdOrderByCreatedAtDesc(repo.getId());

        RunStatus latestBuildStatus = recentRuns.isEmpty()
                ? null
                : parseRunStatus(recentRuns.get(0).getStatus());

        List<RunStatus> sparkline = recentRuns.stream()
                .limit(10)
                .map(run -> parseRunStatus(run.getStatus()))
                .toList();

        return new CatalogRepoTileDto(
                repo.getId(),
                repo.getFullName(),
                repo.getProjectId(),
                resolveProjectName(repo.getProjectId()),
                repo.getWorkspaceId(),
                repo.getDefaultBranch(),
                CodeBuildManagementEnums.parse(RepoVisibility.class, repo.getVisibility(), "visibility"),
                latestBuildStatus,
                sparkline,
                repo.getLastActivityAt(),
                repo.getExternalUrl()
        );
    }

    private String resolveProjectName(String projectId) {
        if (projectId == null) {
            return "Unassigned";
        }
        try {
            return projectSpaceSeedCatalog.project(projectId).name();
        } catch (Exception ex) {
            return projectId;
        }
    }

    private RunStatus parseRunStatus(String status) {
        if (status == null || status.isBlank()) {
            return RunStatus.NEUTRAL;
        }
        try {
            return RunStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return RunStatus.NEUTRAL;
        }
    }

    private boolean matchesVisibility(RepoEntity repo, String visibility) {
        return visibility == null || visibility.isBlank()
                || repo.getVisibility().equalsIgnoreCase(visibility);
    }

    private boolean matchesBuildStatus(CatalogRepoTileDto tile, String buildStatus) {
        if (buildStatus == null || buildStatus.isBlank()) {
            return true;
        }
        if (tile.latestBuildStatus() == null) {
            return false;
        }
        return tile.latestBuildStatus().name().equalsIgnoreCase(buildStatus);
    }

    private boolean matchesSearch(CatalogRepoTileDto tile, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return Stream.of(tile.fullName(), tile.projectName(), tile.defaultBranch())
                .filter(value -> value != null)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(needle));
    }
}
