package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCheckRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.PrReviewRowDto;
import com.sdlctower.domain.codebuildmanagement.policy.BlockerVisibilityPolicy;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.projection.PrAiReviewProjection;
import com.sdlctower.domain.codebuildmanagement.projection.PrChecksProjection;
import com.sdlctower.domain.codebuildmanagement.projection.PrCommitsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.PrHeaderProjection;
import com.sdlctower.domain.codebuildmanagement.projection.PrReviewsProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementPrDetailService")
public class PrDetailService {

    private final PrHeaderProjection headerProjection;
    private final PrChecksProjection checksProjection;
    private final PrReviewsProjection reviewsProjection;
    private final PrCommitsProjection commitsProjection;
    private final PrAiReviewProjection aiReviewProjection;
    private final BlockerVisibilityPolicy blockerVisibilityPolicy;
    private final CodeBuildAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public PrDetailService(
            PrHeaderProjection headerProjection,
            PrChecksProjection checksProjection,
            PrReviewsProjection reviewsProjection,
            PrCommitsProjection commitsProjection,
            PrAiReviewProjection aiReviewProjection,
            BlockerVisibilityPolicy blockerVisibilityPolicy,
            CodeBuildAccessGuard accessGuard
    ) {
        this(headerProjection, checksProjection, reviewsProjection, commitsProjection,
                aiReviewProjection, blockerVisibilityPolicy, accessGuard, ForkJoinPool.commonPool());
    }

    PrDetailService(
            PrHeaderProjection headerProjection,
            PrChecksProjection checksProjection,
            PrReviewsProjection reviewsProjection,
            PrCommitsProjection commitsProjection,
            PrAiReviewProjection aiReviewProjection,
            BlockerVisibilityPolicy blockerVisibilityPolicy,
            CodeBuildAccessGuard accessGuard,
            Executor executor
    ) {
        this.headerProjection = headerProjection;
        this.checksProjection = checksProjection;
        this.reviewsProjection = reviewsProjection;
        this.commitsProjection = commitsProjection;
        this.aiReviewProjection = aiReviewProjection;
        this.blockerVisibilityPolicy = blockerVisibilityPolicy;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public PrDetailAggregateDto loadAggregate(String prId, String memberId) {
        PrHeaderDto header = loadHeader(prId);

        CompletableFuture<SectionResultDto<PrHeaderDto>> headerFuture = CompletableFuture.completedFuture(SectionResultDto.ok(header));
        CompletableFuture<SectionResultDto<List<PrCheckRowDto>>> checks = loadAsync(() -> loadChecks(prId), "checks");
        CompletableFuture<SectionResultDto<List<PrReviewRowDto>>> reviews = loadAsync(() -> loadReviews(prId), "reviews");
        CompletableFuture<SectionResultDto<List<PrCommitRowDto>>> commits = loadAsync(() -> loadCommits(prId), "commits");
        CompletableFuture<SectionResultDto<AiPrReviewDto>> aiReview = loadAsync(() -> loadAiReview(prId, memberId), "aiReview");

        CompletableFuture.allOf(checks, reviews, commits, aiReview)
                .completeOnTimeout(null, CodeBuildManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new PrDetailAggregateDto(headerFuture.join(), checks.join(), reviews.join(), commits.join(), aiReview.join());
    }

    public PrHeaderDto loadHeader(String prId) {
        return headerProjection.load(prId);
    }

    public List<PrCheckRowDto> loadChecks(String prId) {
        return checksProjection.load(prId);
    }

    public List<PrReviewRowDto> loadReviews(String prId) {
        return reviewsProjection.load(prId);
    }

    public List<PrCommitRowDto> loadCommits(String prId) {
        return commitsProjection.load(prId);
    }

    public AiPrReviewDto loadAiReview(String prId, String memberId) {
        AiPrReviewDto review = aiReviewProjection.load(prId);
        boolean canSeeBlockerBodies = accessGuard.isAdmin(memberId);
        return blockerVisibilityPolicy.filter(review, canSeeBlockerBodies);
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(Supplier<T> supplier, String label) {
        return CompletableFuture.supplyAsync(supplier, executor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(capitalize(label) + " projection timed out"),
                        CodeBuildManagementConstants.PROJECTION_BUDGET.toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(ex -> SectionResultDto.fail(capitalize(label) + " projection failed: " + rootCause(ex).getMessage()));
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
