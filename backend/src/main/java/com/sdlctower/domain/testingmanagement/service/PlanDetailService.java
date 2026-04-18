package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiDraftRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiInsightsDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanCaseRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RecentRunRowDto;
import com.sdlctower.domain.testingmanagement.policy.TestingAccessGuard;
import com.sdlctower.domain.testingmanagement.projection.PlanAiInsightsProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanCasesProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanCoverageProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanDraftInboxProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanHeaderProjection;
import com.sdlctower.domain.testingmanagement.projection.PlanRecentRunsProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("testingManagementPlanDetailService")
public class PlanDetailService {

    private final PlanHeaderProjection headerProjection;
    private final PlanCasesProjection casesProjection;
    private final PlanCoverageProjection coverageProjection;
    private final PlanRecentRunsProjection recentRunsProjection;
    private final PlanDraftInboxProjection draftInboxProjection;
    private final PlanAiInsightsProjection aiInsightsProjection;
    private final TestingAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public PlanDetailService(
            PlanHeaderProjection headerProjection,
            PlanCasesProjection casesProjection,
            PlanCoverageProjection coverageProjection,
            PlanRecentRunsProjection recentRunsProjection,
            PlanDraftInboxProjection draftInboxProjection,
            PlanAiInsightsProjection aiInsightsProjection,
            TestingAccessGuard accessGuard
    ) {
        this(
                headerProjection,
                casesProjection,
                coverageProjection,
                recentRunsProjection,
                draftInboxProjection,
                aiInsightsProjection,
                accessGuard,
                ForkJoinPool.commonPool()
        );
    }

    PlanDetailService(
            PlanHeaderProjection headerProjection,
            PlanCasesProjection casesProjection,
            PlanCoverageProjection coverageProjection,
            PlanRecentRunsProjection recentRunsProjection,
            PlanDraftInboxProjection draftInboxProjection,
            PlanAiInsightsProjection aiInsightsProjection,
            TestingAccessGuard accessGuard,
            Executor executor
    ) {
        this.headerProjection = headerProjection;
        this.casesProjection = casesProjection;
        this.coverageProjection = coverageProjection;
        this.recentRunsProjection = recentRunsProjection;
        this.draftInboxProjection = draftInboxProjection;
        this.aiInsightsProjection = aiInsightsProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public PlanDetailAggregateDto loadAggregate(String planId) {
        PlanHeaderDto header = headerProjection.load(planId);
        accessGuard.requireProjectRead(header.projectId());

        CompletableFuture<SectionResultDto<PlanHeaderDto>> headerSection = loadAsync(() -> loadHeader(planId), "header");
        CompletableFuture<SectionResultDto<List<PlanCaseRowDto>>> cases = loadAsync(() -> loadCases(planId), "cases");
        CompletableFuture<SectionResultDto<List<CoverageRowDto>>> coverage = loadAsync(() -> loadCoverage(planId), "coverage");
        CompletableFuture<SectionResultDto<List<RecentRunRowDto>>> recentRuns = loadAsync(() -> loadRecentRuns(planId), "recent runs");
        CompletableFuture<SectionResultDto<List<AiDraftRowDto>>> draftInbox = loadAsync(() -> loadDraftInbox(planId), "draft inbox");
        CompletableFuture<SectionResultDto<AiInsightsDto>> aiInsights = loadAsync(() -> loadAiInsights(planId), "ai insights");

        CompletableFuture.allOf(headerSection, cases, coverage, recentRuns, draftInbox, aiInsights)
                .completeOnTimeout(null, TestingManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new PlanDetailAggregateDto(
                headerSection.join(),
                cases.join(),
                coverage.join(),
                recentRuns.join(),
                draftInbox.join(),
                aiInsights.join()
        );
    }

    public PlanHeaderDto loadHeader(String planId) {
        PlanHeaderDto header = headerProjection.load(planId);
        accessGuard.requireProjectRead(header.projectId());
        return header;
    }

    public List<PlanCaseRowDto> loadCases(String planId) {
        accessGuard.requireProjectRead(headerProjection.load(planId).projectId());
        return casesProjection.load(planId);
    }

    public List<CoverageRowDto> loadCoverage(String planId) {
        accessGuard.requireProjectRead(headerProjection.load(planId).projectId());
        return coverageProjection.load(planId);
    }

    public List<RecentRunRowDto> loadRecentRuns(String planId) {
        accessGuard.requireProjectRead(headerProjection.load(planId).projectId());
        return recentRunsProjection.load(planId);
    }

    public List<AiDraftRowDto> loadDraftInbox(String planId) {
        accessGuard.requireProjectRead(headerProjection.load(planId).projectId());
        return draftInboxProjection.load(planId);
    }

    public AiInsightsDto loadAiInsights(String planId) {
        accessGuard.requireProjectRead(headerProjection.load(planId).projectId());
        return aiInsightsProjection.load(planId);
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(Supplier<T> supplier, String label) {
        return CompletableFuture.supplyAsync(supplier, executor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(capitalize(label) + " projection timed out"),
                        TestingManagementConstants.PROJECTION_BUDGET.toMillis(),
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
