package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.testingmanagement.TestingManagementConstants;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCaseResultRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCoverageDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunDetailAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunHeaderDto;
import com.sdlctower.domain.testingmanagement.policy.TestingAccessGuard;
import com.sdlctower.domain.testingmanagement.projection.RunCaseResultsProjection;
import com.sdlctower.domain.testingmanagement.projection.RunCoverageProjection;
import com.sdlctower.domain.testingmanagement.projection.RunHeaderProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("testingManagementRunDetailService")
public class RunDetailService {

    private final RunHeaderProjection runHeaderProjection;
    private final RunCaseResultsProjection runCaseResultsProjection;
    private final RunCoverageProjection runCoverageProjection;
    private final TestingAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public RunDetailService(
            RunHeaderProjection runHeaderProjection,
            RunCaseResultsProjection runCaseResultsProjection,
            RunCoverageProjection runCoverageProjection,
            TestingAccessGuard accessGuard
    ) {
        this(runHeaderProjection, runCaseResultsProjection, runCoverageProjection, accessGuard, ForkJoinPool.commonPool());
    }

    RunDetailService(
            RunHeaderProjection runHeaderProjection,
            RunCaseResultsProjection runCaseResultsProjection,
            RunCoverageProjection runCoverageProjection,
            TestingAccessGuard accessGuard,
            Executor executor
    ) {
        this.runHeaderProjection = runHeaderProjection;
        this.runCaseResultsProjection = runCaseResultsProjection;
        this.runCoverageProjection = runCoverageProjection;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public RunDetailAggregateDto loadAggregate(String runId) {
        RunHeaderDto header = runHeaderProjection.load(runId);
        accessGuard.requireProjectRead(header.projectId());

        CompletableFuture<SectionResultDto<RunHeaderDto>> headerSection = loadAsync(() -> loadHeader(runId), "header");
        CompletableFuture<SectionResultDto<List<RunCaseResultRowDto>>> caseResults = loadAsync(() -> loadCaseResults(runId), "case results");
        CompletableFuture<SectionResultDto<RunCoverageDto>> coverage = loadAsync(() -> loadCoverage(runId), "coverage");

        CompletableFuture.allOf(headerSection, caseResults, coverage)
                .completeOnTimeout(null, TestingManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new RunDetailAggregateDto(headerSection.join(), caseResults.join(), coverage.join());
    }

    public RunHeaderDto loadHeader(String runId) {
        return runHeaderProjection.load(runId);
    }

    public List<RunCaseResultRowDto> loadCaseResults(String runId) {
        return runCaseResultsProjection.load(runId);
    }

    public RunCoverageDto loadCoverage(String runId) {
        return runCoverageProjection.load(runId);
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
