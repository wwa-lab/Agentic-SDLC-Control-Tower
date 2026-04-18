package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoAiSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.AiWorkspaceSummaryEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.AiWorkspaceSummaryRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoAiSummaryProjection")
public class RepoAiSummaryProjection {

    private final AiWorkspaceSummaryRepository aiWorkspaceSummaryRepository;
    private final RepoRepository repoRepository;

    public RepoAiSummaryProjection(
            AiWorkspaceSummaryRepository aiWorkspaceSummaryRepository,
            RepoRepository repoRepository
    ) {
        this.aiWorkspaceSummaryRepository = aiWorkspaceSummaryRepository;
        this.repoRepository = repoRepository;
    }

    public RepoAiSummaryDto load(String repoId) {
        RepoEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + repoId));

        return aiWorkspaceSummaryRepository
                .findTopByWorkspaceIdAndRepoIdOrderByGeneratedAtDesc(
                        repo.getWorkspaceId(), repoId)
                .map(this::toDto)
                .orElse(new RepoAiSummaryDto(
                        AiRowStatus.PENDING,
                        null,
                        null,
                        null,
                        null
                ));
    }

    private RepoAiSummaryDto toDto(AiWorkspaceSummaryEntity entity) {
        return new RepoAiSummaryDto(
                CodeBuildManagementEnums.parse(AiRowStatus.class, entity.getStatus(), "status"),
                entity.getNarrative(),
                entity.getSkillVersion(),
                entity.getGeneratedAt(),
                null
        );
    }
}
