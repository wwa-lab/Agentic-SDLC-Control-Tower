package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.StoryChipDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunStatus;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.RunTrigger;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineRunRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunHeaderProjection")
public class RunHeaderProjection {

    private final PipelineRunRepository pipelineRunRepository;
    private final RepoRepository repoRepository;
    private final GitCommitRepository gitCommitRepository;
    private final CommitStoryLinkRepository commitStoryLinkRepository;

    public RunHeaderProjection(
            PipelineRunRepository pipelineRunRepository,
            RepoRepository repoRepository,
            GitCommitRepository gitCommitRepository,
            CommitStoryLinkRepository commitStoryLinkRepository
    ) {
        this.pipelineRunRepository = pipelineRunRepository;
        this.repoRepository = repoRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.commitStoryLinkRepository = commitStoryLinkRepository;
    }

    public RunHeaderDto load(String runId) {
        PipelineRunEntity run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_RUN_NOT_FOUND", "Run not found: " + runId));

        RepoEntity repo = repoRepository.findById(run.getRepoId())
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + run.getRepoId()));

        List<StoryChipDto> storyChips = resolveStoryChips(run);

        return new RunHeaderDto(
                run.getId(),
                run.getRunNumber(),
                run.getPipelineName(),
                repo.getId(),
                repo.getFullName(),
                CodeBuildManagementEnums.parse(RunStatus.class, run.getStatus(), "status"),
                CodeBuildManagementEnums.parse(RunTrigger.class, run.getTrigger(), "trigger"),
                run.getActor(),
                run.getBranch(),
                run.getHeadSha(),
                run.getDurationSec(),
                run.getStartedAt(),
                run.getCompletedAt(),
                storyChips,
                run.getExternalUrl()
        );
    }

    private List<StoryChipDto> resolveStoryChips(PipelineRunEntity run) {
        if (run.getHeadSha() == null || run.getRepoId() == null) {
            return List.of();
        }
        return gitCommitRepository.findByRepoIdAndSha(run.getRepoId(), run.getHeadSha())
                .map(commit -> {
                    List<CommitStoryLinkEntity> links = commitStoryLinkRepository
                            .findByCommitId(commit.getId());
                    return links.stream()
                            .map(link -> new StoryChipDto(
                                    link.getStoryId(),
                                    CodeBuildManagementEnums.parse(
                                            StoryLinkStatus.class, link.getLinkStatus(), "linkStatus"),
                                    null,
                                    null
                            ))
                            .toList();
                })
                .orElse(List.of());
    }
}
