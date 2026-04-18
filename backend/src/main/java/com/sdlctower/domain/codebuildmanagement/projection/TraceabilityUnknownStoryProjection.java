package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityCommitRefDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementTraceabilityUnknownStoryProjection")
public class TraceabilityUnknownStoryProjection {

    private final CommitStoryLinkRepository commitStoryLinkRepository;
    private final GitCommitRepository gitCommitRepository;
    private final RepoRepository repoRepository;
    private final PipelineRunRepository pipelineRunRepository;

    public TraceabilityUnknownStoryProjection(
            CommitStoryLinkRepository commitStoryLinkRepository,
            GitCommitRepository gitCommitRepository,
            RepoRepository repoRepository,
            PipelineRunRepository pipelineRunRepository
    ) {
        this.commitStoryLinkRepository = commitStoryLinkRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.repoRepository = repoRepository;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    public List<TraceabilityCommitRefDto> load(String workspaceId) {
        List<CommitStoryLinkEntity> links = commitStoryLinkRepository
                .findByLinkStatus("UNKNOWN_STORY", PageRequest.of(0, 100));

        return links.stream()
                .map(this::toCommitRef)
                .filter(ref -> ref != null)
                .toList();
    }

    private TraceabilityCommitRefDto toCommitRef(CommitStoryLinkEntity link) {
        return gitCommitRepository.findById(link.getCommitId())
                .map(commit -> {
                    String repoFullName = resolveRepoFullName(commit.getRepoId());
                    String buildRunId = resolveBuildRunId(commit);
                    RunStatus buildStatus = resolveBuildStatus(commit);

                    return new TraceabilityCommitRefDto(
                            commit.getSha(),
                            commit.getShortSha(),
                            commit.getAuthor(),
                            commit.getMessage(),
                            repoFullName,
                            StoryLinkStatus.UNKNOWN_STORY,
                            buildRunId,
                            buildStatus,
                            commit.getCommittedAt()
                    );
                })
                .orElse(null);
    }

    private String resolveRepoFullName(String repoId) {
        return repoRepository.findById(repoId)
                .map(RepoEntity::getFullName)
                .orElse(repoId);
    }

    private String resolveBuildRunId(GitCommitEntity commit) {
        if (commit.getSha() == null) {
            return null;
        }
        return pipelineRunRepository.findByRepoIdAndHeadSha(commit.getRepoId(), commit.getSha())
                .map(PipelineRunEntity::getId)
                .orElse(null);
    }

    private RunStatus resolveBuildStatus(GitCommitEntity commit) {
        if (commit.getSha() == null) {
            return null;
        }
        return pipelineRunRepository.findByRepoIdAndHeadSha(commit.getRepoId(), commit.getSha())
                .map(run -> parseRunStatus(run.getStatus()))
                .orElse(null);
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
}
