package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityCommitRefDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilityStoryRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildAccessGuard;
import com.sdlctower.domain.codebuildmanagement.projection.TraceabilityNoStoryIdProjection;
import com.sdlctower.domain.codebuildmanagement.projection.TraceabilityStoryRowsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.TraceabilitySummaryProjection;
import com.sdlctower.domain.codebuildmanagement.projection.TraceabilityUnknownStoryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementTraceabilityService")
public class TraceabilityService {

    private final TraceabilitySummaryProjection summaryProjection;
    private final TraceabilityStoryRowsProjection storyRowsProjection;
    private final TraceabilityUnknownStoryProjection unknownStoryProjection;
    private final TraceabilityNoStoryIdProjection noStoryIdProjection;
    private final CodeBuildAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public TraceabilityService(
            TraceabilitySummaryProjection summaryProjection,
            TraceabilityStoryRowsProjection storyRowsProjection,
            TraceabilityUnknownStoryProjection unknownStoryProjection,
            TraceabilityNoStoryIdProjection noStoryIdProjection,
            CodeBuildAccessGuard accessGuard
    ) {
        this(summaryProjection, storyRowsProjection, unknownStoryProjection, noStoryIdProjection,
                accessGuard, ForkJoinPool.commonPool());
    }

    TraceabilityService(
            TraceabilitySummaryProjection summaryProjection,
            TraceabilityStoryRowsProjection storyRowsProjection,
            TraceabilityUnknownStoryProjection unknownStoryProjection,
            TraceabilityNoStoryIdProjection noStoryIdProjection,
            CodeBuildAccessGuard accessGuard,
            Executor executor
    ) {
        this.summaryProjection = summaryProjection;
        this.storyRowsProjection = storyRowsProjection;
        this.unknownStoryProjection = unknownStoryProjection;
        this.noStoryIdProjection = noStoryIdProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public TraceabilityAggregateDto loadAggregate(String workspaceId, String projectId, String linkStatus, String storyState) {
        accessGuard.requireWorkspaceRead(workspaceId);

        CompletableFuture<SectionResultDto<TraceabilitySummaryDto>> summary =
                loadAsync(() -> loadSummary(workspaceId), "summary");
        CompletableFuture<SectionResultDto<List<TraceabilityStoryRowDto>>> storyRows =
                loadAsync(() -> loadStoryRows(workspaceId, projectId), "storyRows");
        CompletableFuture<SectionResultDto<List<TraceabilityCommitRefDto>>> unknownStory =
                loadAsync(() -> loadUnknownStoryRows(workspaceId), "unknownStory");
        CompletableFuture<SectionResultDto<List<TraceabilityCommitRefDto>>> noStoryId =
                loadAsync(() -> loadNoStoryIdRows(workspaceId), "noStoryId");

        CompletableFuture.allOf(summary, storyRows, unknownStory, noStoryId)
                .completeOnTimeout(null, CodeBuildManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new TraceabilityAggregateDto(summary.join(), storyRows.join(), unknownStory.join(), noStoryId.join());
    }

    public TraceabilitySummaryDto loadSummary(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return summaryProjection.load(workspaceId);
    }

    public List<TraceabilityStoryRowDto> loadStoryRows(String workspaceId, String projectId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return storyRowsProjection.load(workspaceId, projectId);
    }

    public List<TraceabilityCommitRefDto> loadUnknownStoryRows(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return unknownStoryProjection.load(workspaceId);
    }

    public List<TraceabilityCommitRefDto> loadNoStoryIdRows(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return noStoryIdProjection.load(workspaceId);
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
