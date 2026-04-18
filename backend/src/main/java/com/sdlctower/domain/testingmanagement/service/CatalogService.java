package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogPlanRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.testingmanagement.policy.TestingAccessGuard;
import com.sdlctower.domain.testingmanagement.projection.CatalogFiltersProjection;
import com.sdlctower.domain.testingmanagement.projection.CatalogGridProjection;
import com.sdlctower.domain.testingmanagement.projection.CatalogSummaryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("testingManagementCatalogService")
public class CatalogService {

    private final CatalogSummaryProjection summaryProjection;
    private final CatalogGridProjection gridProjection;
    private final CatalogFiltersProjection filtersProjection;
    private final TestingAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public CatalogService(
            CatalogSummaryProjection summaryProjection,
            CatalogGridProjection gridProjection,
            CatalogFiltersProjection filtersProjection,
            TestingAccessGuard accessGuard
    ) {
        this(summaryProjection, gridProjection, filtersProjection, accessGuard, ForkJoinPool.commonPool());
    }

    CatalogService(
            CatalogSummaryProjection summaryProjection,
            CatalogGridProjection gridProjection,
            CatalogFiltersProjection filtersProjection,
            TestingAccessGuard accessGuard,
            Executor executor
    ) {
        this.summaryProjection = summaryProjection;
        this.gridProjection = gridProjection;
        this.filtersProjection = filtersProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public CatalogAggregateDto loadAggregate(String workspaceId, String projectId, String planState, String coverageLed, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);
        CompletableFuture<SectionResultDto<CatalogSummaryDto>> summary = loadAsync(() -> loadSummary(workspaceId, projectId, planState, coverageLed, search), "summary");
        CompletableFuture<SectionResultDto<List<CatalogPlanRowDto>>> grid = loadAsync(() -> loadGrid(workspaceId, projectId, planState, coverageLed, search), "grid");
        CompletableFuture<SectionResultDto<CatalogFiltersDto>> filters = loadAsync(() -> loadFilters(workspaceId), "filters");

        CompletableFuture.allOf(summary, grid, filters)
                .completeOnTimeout(null, TestingManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new CatalogAggregateDto(summary.join(), grid.join(), filters.join());
    }

    public CatalogSummaryDto loadSummary(String workspaceId, String projectId, String planState, String coverageLed, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return summaryProjection.load(workspaceId, projectId, planState, coverageLed, search);
    }

    public List<CatalogPlanRowDto> loadGrid(String workspaceId, String projectId, String planState, String coverageLed, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return gridProjection.load(workspaceId, projectId, planState, coverageLed, search);
    }

    public CatalogFiltersDto loadFilters(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return filtersProjection.load(workspaceId);
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
