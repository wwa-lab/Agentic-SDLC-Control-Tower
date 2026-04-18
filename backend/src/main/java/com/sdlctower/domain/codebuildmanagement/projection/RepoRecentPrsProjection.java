package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiNoteCountsDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentPrRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiNoteSeverity;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.PrState;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewNoteEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewNoteRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRepoRecentPrsProjection")
public class RepoRecentPrsProjection {

    private final PullRequestRepository pullRequestRepository;
    private final AiPrReviewRepository aiPrReviewRepository;
    private final AiPrReviewNoteRepository aiPrReviewNoteRepository;

    public RepoRecentPrsProjection(
            PullRequestRepository pullRequestRepository,
            AiPrReviewRepository aiPrReviewRepository,
            AiPrReviewNoteRepository aiPrReviewNoteRepository
    ) {
        this.pullRequestRepository = pullRequestRepository;
        this.aiPrReviewRepository = aiPrReviewRepository;
        this.aiPrReviewNoteRepository = aiPrReviewNoteRepository;
    }

    public List<RepoRecentPrRowDto> load(String repoId) {
        List<PullRequestEntity> prs = pullRequestRepository
                .findTop15ByRepoIdOrderByUpdatedAtDesc(repoId);

        return prs.stream()
                .map(this::toDto)
                .toList();
    }

    private RepoRecentPrRowDto toDto(PullRequestEntity pr) {
        AiNoteCountsDto noteCounts = computeNoteCounts(pr.getId());

        return new RepoRecentPrRowDto(
                pr.getId(),
                pr.getPrNumber(),
                pr.getTitle(),
                pr.getAuthor(),
                pr.getSourceBranch(),
                pr.getTargetBranch(),
                CodeBuildManagementEnums.parse(PrState.class, pr.getState(), "state"),
                noteCounts,
                pr.getUpdatedAt()
        );
    }

    private AiNoteCountsDto computeNoteCounts(String prId) {
        return aiPrReviewRepository.findByPrIdOrderByGeneratedAtDesc(prId).stream()
                .findFirst()
                .map(review -> {
                    List<AiPrReviewNoteEntity> notes = aiPrReviewNoteRepository
                            .findByReviewId(review.getId());
                    int blocker = 0;
                    int major = 0;
                    int minor = 0;
                    int info = 0;
                    for (AiPrReviewNoteEntity note : notes) {
                        AiNoteSeverity severity = CodeBuildManagementEnums.parse(
                                AiNoteSeverity.class, note.getSeverity(), "severity");
                        switch (severity) {
                            case BLOCKER -> blocker++;
                            case MAJOR -> major++;
                            case MINOR -> minor++;
                            case INFO -> info++;
                        }
                    }
                    return new AiNoteCountsDto(blocker, major, minor, info);
                })
                .orElse(new AiNoteCountsDto(0, 0, 0, 0));
    }
}
