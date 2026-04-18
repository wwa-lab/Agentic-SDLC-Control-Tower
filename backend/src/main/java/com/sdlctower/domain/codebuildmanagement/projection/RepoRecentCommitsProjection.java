package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.StoryChipDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CommitStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.GitCommitRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoRecentCommitsProjection")
public class RepoRecentCommitsProjection {

    private final GitCommitRepository gitCommitRepository;
    private final CommitStoryLinkRepository commitStoryLinkRepository;
    private final RepoRepository repoRepository;

    public RepoRecentCommitsProjection(
            GitCommitRepository gitCommitRepository,
            CommitStoryLinkRepository commitStoryLinkRepository,
            RepoRepository repoRepository
    ) {
        this.gitCommitRepository = gitCommitRepository;
        this.commitStoryLinkRepository = commitStoryLinkRepository;
        this.repoRepository = repoRepository;
    }

    public List<RepoRecentCommitRowDto> load(String repoId) {
        RepoEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + repoId));

        String defaultBranch = repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "main";
        List<GitCommitEntity> commits = gitCommitRepository
                .findTop20ByRepoIdAndBranchNameOrderByCommittedAtDesc(repoId, defaultBranch);

        return commits.stream()
                .map(this::toDto)
                .toList();
    }

    private RepoRecentCommitRowDto toDto(GitCommitEntity commit) {
        List<CommitStoryLinkEntity> links = commitStoryLinkRepository
                .findByCommitId(commit.getId());

        List<StoryChipDto> storyChips = links.stream()
                .map(this::toStoryChip)
                .toList();

        return new RepoRecentCommitRowDto(
                commit.getSha(),
                commit.getShortSha(),
                commit.getAuthor(),
                commit.getMessage(),
                commit.getCommittedAt(),
                storyChips
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
