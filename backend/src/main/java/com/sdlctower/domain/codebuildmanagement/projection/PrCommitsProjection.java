package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.StoryChipDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementPrCommitsProjection")
public class PrCommitsProjection {

    private final PullRequestRepository pullRequestRepository;
    private final GitCommitRepository gitCommitRepository;
    private final CommitStoryLinkRepository commitStoryLinkRepository;

    public PrCommitsProjection(
            PullRequestRepository pullRequestRepository,
            GitCommitRepository gitCommitRepository,
            CommitStoryLinkRepository commitStoryLinkRepository
    ) {
        this.pullRequestRepository = pullRequestRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.commitStoryLinkRepository = commitStoryLinkRepository;
    }

    public List<PrCommitRowDto> load(String prId) {
        PullRequestEntity pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_PR_NOT_FOUND", "Pull request not found: " + prId));

        List<GitCommitEntity> commits = gitCommitRepository
                .findTop20ByRepoIdAndBranchNameOrderByCommittedAtDesc(
                        pr.getRepoId(), pr.getSourceBranch());

        return commits.stream()
                .map(this::toDto)
                .toList();
    }

    private PrCommitRowDto toDto(GitCommitEntity commit) {
        List<CommitStoryLinkEntity> links = commitStoryLinkRepository
                .findByCommitId(commit.getId());

        List<StoryChipDto> storyChips = links.stream()
                .map(this::toStoryChip)
                .toList();

        return new PrCommitRowDto(
                commit.getSha(),
                commit.getShortSha(),
                commit.getAuthor(),
                commit.getMessage(),
                storyChips,
                commit.getCommittedAt()
        );
    }

    private StoryChipDto toStoryChip(CommitStoryLinkEntity link) {
        StoryLinkStatus status = CodeBuildManagementEnums.parse(
                StoryLinkStatus.class, link.getLinkStatus(), "linkStatus");
        return new StoryChipDto(
                link.getStoryId(),
                status,
                null,
                null
        );
    }
}
