package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.StoryChipDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.PrState;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.StoryLinkStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.PrStoryLinkEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PrStoryLinkRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.RepoRepository;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementPrHeaderProjection")
public class PrHeaderProjection {

    private final PullRequestRepository pullRequestRepository;
    private final RepoRepository repoRepository;
    private final PrStoryLinkRepository prStoryLinkRepository;

    public PrHeaderProjection(
            PullRequestRepository pullRequestRepository,
            RepoRepository repoRepository,
            PrStoryLinkRepository prStoryLinkRepository
    ) {
        this.pullRequestRepository = pullRequestRepository;
        this.repoRepository = repoRepository;
        this.prStoryLinkRepository = prStoryLinkRepository;
    }

    public PrHeaderDto load(String prId) {
        PullRequestEntity pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_PR_NOT_FOUND", "Pull request not found: " + prId));

        RepoEntity repo = repoRepository.findById(pr.getRepoId())
                .orElseThrow(() -> CodeBuildManagementException.notFound(
                        "CB_REPO_NOT_FOUND", "Repo not found: " + pr.getRepoId()));

        List<PrStoryLinkEntity> storyLinks = prStoryLinkRepository.findByPrId(prId);
        List<StoryChipDto> storyChips = storyLinks.stream()
                .map(this::toStoryChip)
                .toList();

        return new PrHeaderDto(
                pr.getId(),
                pr.getPrNumber(),
                repo.getId(),
                repo.getFullName(),
                pr.getTitle(),
                pr.getAuthor(),
                pr.getSourceBranch(),
                pr.getTargetBranch(),
                CodeBuildManagementEnums.parse(PrState.class, pr.getState(), "state"),
                pr.getHeadSha(),
                storyChips,
                pr.getCreatedAt(),
                pr.getUpdatedAt(),
                pr.getExternalUrl()
        );
    }

    private StoryChipDto toStoryChip(PrStoryLinkEntity link) {
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
