package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.projection.CatalogGridProjection;
import com.sdlctower.domain.codebuildmanagement.projection.CatalogSummaryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementCatalogService")
public class CatalogService {

    private final CatalogSummaryProjection summaryProjection;
    private final CatalogGridProjection gridProjection;
    private final CodeBuildAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public CatalogService(
            CatalogSummaryProjection summaryProjection,
            CatalogGridProjection gridProjection,
            CodeBuildAccessGuard accessGuard
    ) {
        this(summaryProjection, gridProjection, accessGuard, ForkJoinPool.commonPool());
    }

    CatalogService(
            CatalogSummaryProjection summaryProjection,
            CatalogGridProjection gridProjection,
            CodeBuildAccessGuard accessGuard,
            Executor executor
    ) {
        this.summaryProjection = summaryProjection;
        this.gridProjection = gridProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public CatalogAggregateDto loadAggregate(String workspaceId, String projectId, String buildStatus, String visibility, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);

        CompletableFuture<SectionResultDto<CatalogSummaryDto>> summary =
                loadAsync(() -> loadSummary(workspaceId, projectId, buildStatus, visibility, search), "summary");
        CompletableFuture<SectionResultDto<List<CatalogSectionDto>>> grid =
                loadAsync(() -> loadGrid(workspaceId, projectId, buildStatus, visibility, search), "grid");
        CompletableFuture<SectionResultDto<CatalogFiltersDto>> filters =
                loadAsync(() -> loadFilters(workspaceId), "filters");

        CompletableFuture.allOf(summary, grid, filters)
                .completeOnTimeout(null, CodeBuildManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new CatalogAggregateDto(summary.join(), grid.join(), filters.join());
    }

    public CatalogSummaryDto loadSummary(String workspaceId, String projectId, String buildStatus, String visibility, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return summaryProjection.load(workspaceId, projectId, buildStatus, visibility, search);
    }

    public List<CatalogSectionDto> loadGrid(String workspaceId, String projectId, String buildStatus, String visibility, String search) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return gridProjection.load(workspaceId, projectId, buildStatus, visibility, search);
    }

    public CatalogFiltersDto loadFilters(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return gridProjection.loadFilters(workspaceId);
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
