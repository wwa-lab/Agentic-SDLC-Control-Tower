package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RepoVisibility;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoHeaderProjection")
public class RepoHeaderProjection {

    private final RepoRepository repoRepository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;
    private final TeamSpaceSeedCatalog teamSpaceSeedCatalog;

    public RepoHeaderProjection(
            RepoRepository repoRepository,
            ProjectSpaceSeedCatalog projectSpaceSeedCatalog,
            TeamSpaceSeedCatalog teamSpaceSeedCatalog
    ) {
        this.repoRepository = repoRepository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
        this.teamSpaceSeedCatalog = teamSpaceSeedCatalog;
    }

    public RepoHeaderDto load(String repoId) {
        RepoEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + repoId));

        return new RepoHeaderDto(
                repo.getId(),
                repo.getFullName(),
                repo.getProjectId(),
                resolveProjectName(repo.getProjectId()),
                repo.getWorkspaceId(),
                resolveWorkspaceName(repo.getWorkspaceId()),
                repo.getDefaultBranch(),
                CodeBuildManagementEnums.parse(RepoVisibility.class, repo.getVisibility(), "visibility"),
                repo.getLastSyncedAt(),
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

    private String resolveWorkspaceName(String workspaceId) {
        if (workspaceId == null) {
            return "Unknown";
        }
        try {
            return teamSpaceSeedCatalog.workspace(workspaceId).name();
        } catch (Exception ex) {
            return workspaceId;
        }
    }
}
