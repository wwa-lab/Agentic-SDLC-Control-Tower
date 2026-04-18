package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoAiSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoBranchDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoHealthSummaryDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentCommitRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentPrRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RepoRecentRunRowDto;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.projection.RepoAiSummaryProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoBranchesProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoHeaderProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoHealthSummaryProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoRecentCommitsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoRecentPrsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RepoRecentRunsProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementRepoDetailService")
public class RepoDetailService {

    private final RepoHeaderProjection headerProjection;
    private final RepoBranchesProjection branchesProjection;
    private final RepoRecentRunsProjection recentRunsProjection;
    private final RepoRecentPrsProjection recentPrsProjection;
    private final RepoRecentCommitsProjection recentCommitsProjection;
    private final RepoHealthSummaryProjection healthSummaryProjection;
    private final RepoAiSummaryProjection aiSummaryProjection;
    private final CodeBuildAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public RepoDetailService(
            RepoHeaderProjection headerProjection,
            RepoBranchesProjection branchesProjection,
            RepoRecentRunsProjection recentRunsProjection,
            RepoRecentPrsProjection recentPrsProjection,
            RepoRecentCommitsProjection recentCommitsProjection,
            RepoHealthSummaryProjection healthSummaryProjection,
            RepoAiSummaryProjection aiSummaryProjection,
            CodeBuildAccessGuard accessGuard
    ) {
        this(headerProjection, branchesProjection, recentRunsProjection, recentPrsProjection,
                recentCommitsProjection, healthSummaryProjection, aiSummaryProjection, accessGuard,
                ForkJoinPool.commonPool());
    }

    RepoDetailService(
            RepoHeaderProjection headerProjection,
            RepoBranchesProjection branchesProjection,
            RepoRecentRunsProjection recentRunsProjection,
            RepoRecentPrsProjection recentPrsProjection,
            RepoRecentCommitsProjection recentCommitsProjection,
            RepoHealthSummaryProjection healthSummaryProjection,
            RepoAiSummaryProjection aiSummaryProjection,
            CodeBuildAccessGuard accessGuard,
            Executor executor
    ) {
        this.headerProjection = headerProjection;
        this.branchesProjection = branchesProjection;
        this.recentRunsProjection = recentRunsProjection;
        this.recentPrsProjection = recentPrsProjection;
        this.recentCommitsProjection = recentCommitsProjection;
        this.healthSummaryProjection = healthSummaryProjection;
        this.aiSummaryProjection = aiSummaryProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public RepoDetailAggregateDto loadAggregate(String repoId) {
        RepoHeaderDto header = loadHeader(repoId);
        accessGuard.requireRead(header.projectId());

        CompletableFuture<SectionResultDto<RepoHeaderDto>> headerFuture = CompletableFuture.completedFuture(SectionResultDto.ok(header));
        CompletableFuture<SectionResultDto<List<RepoBranchDto>>> branches = loadAsync(() -> loadBranches(repoId), "branches");
        CompletableFuture<SectionResultDto<List<RepoRecentRunRowDto>>> runs = loadAsync(() -> loadRecentRuns(repoId), "recentRuns");
        CompletableFuture<SectionResultDto<List<RepoRecentPrRowDto>>> prs = loadAsync(() -> loadRecentPrs(repoId), "recentPrs");
        CompletableFuture<SectionResultDto<List<RepoRecentCommitRowDto>>> commits = loadAsync(() -> loadRecentCommits(repoId), "recentCommits");
        CompletableFuture<SectionResultDto<RepoHealthSummaryDto>> health = loadAsync(() -> loadHealthSummary(repoId), "healthSummary");
        CompletableFuture<SectionResultDto<RepoAiSummaryDto>> ai = loadAsync(() -> loadAiSummary(repoId), "aiSummary");

        CompletableFuture.allOf(branches, runs, prs, commits, health, ai)
                .completeOnTimeout(null, CodeBuildManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new RepoDetailAggregateDto(
                headerFuture.join(), branches.join(), runs.join(), prs.join(),
                commits.join(), health.join(), ai.join()
        );
    }

    public RepoHeaderDto loadHeader(String repoId) {
        return headerProjection.load(repoId);
    }

    public List<RepoBranchDto> loadBranches(String repoId) {
        return branchesProjection.load(repoId);
    }

    public List<RepoRecentRunRowDto> loadRecentRuns(String repoId) {
        return recentRunsProjection.load(repoId);
    }

    public List<RepoRecentPrRowDto> loadRecentPrs(String repoId) {
        return recentPrsProjection.load(repoId);
    }

    public List<RepoRecentCommitRowDto> loadRecentCommits(String repoId) {
        return recentCommitsProjection.load(repoId);
    }

    public RepoHealthSummaryDto loadHealthSummary(String repoId) {
        return healthSummaryProjection.load(repoId);
    }

    public RepoAiSummaryDto loadAiSummary(String repoId) {
        return aiSummaryProjection.load(repoId);
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
