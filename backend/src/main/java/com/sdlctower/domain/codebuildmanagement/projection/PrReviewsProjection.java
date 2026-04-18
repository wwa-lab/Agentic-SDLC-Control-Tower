package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrReviewRowDto;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestReviewEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PullRequestReviewRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementPrReviewsProjection")
public class PrReviewsProjection {

    private final PullRequestReviewRepository pullRequestReviewRepository;

    public PrReviewsProjection(PullRequestReviewRepository pullRequestReviewRepository) {
        this.pullRequestReviewRepository = pullRequestReviewRepository;
    }

    public List<PrReviewRowDto> load(String prId) {
        List<PullRequestReviewEntity> reviews = pullRequestReviewRepository
                .findByPrIdOrderBySubmittedAtAsc(prId);

        return reviews.stream()
                .map(this::toDto)
                .toList();
    }

    private PrReviewRowDto toDto(PullRequestReviewEntity review) {
        return new PrReviewRowDto(
                review.getReviewerId(),
                review.getReviewerName(),
                review.getState(),
                review.getBodySummary(),
                review.getSubmittedAt()
        );
    }
}
