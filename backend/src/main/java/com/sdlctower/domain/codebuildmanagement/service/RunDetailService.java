package com.sdlctower.domain.codebuildmanagement.service;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunDetailAggregateDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunHeaderDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunJobRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunRerunDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunStepRowDto;
import com.sdlctower.domain.codebuildmanagement.projection.RunAiTriageProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RunHeaderProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RunJobsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RunLogsProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RunRerunProjection;
import com.sdlctower.domain.codebuildmanagement.projection.RunStepsProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codeBuildManagementRunDetailService")
public class RunDetailService {

    private final RunHeaderProjection headerProjection;
    private final RunJobsProjection jobsProjection;
    private final RunStepsProjection stepsProjection;
    private final RunLogsProjection logsProjection;
    private final RunAiTriageProjection aiTriageProjection;
    private final RunRerunProjection rerunProjection;
    private final Executor executor;

    @Autowired
    public RunDetailService(
            RunHeaderProjection headerProjection,
            RunJobsProjection jobsProjection,
            RunStepsProjection stepsProjection,
            RunLogsProjection logsProjection,
            RunAiTriageProjection aiTriageProjection,
            RunRerunProjection rerunProjection
    ) {
        this(headerProjection, jobsProjection, stepsProjection, logsProjection,
                aiTriageProjection, rerunProjection, ForkJoinPool.commonPool());
    }

    RunDetailService(
            RunHeaderProjection headerProjection,
            RunJobsProjection jobsProjection,
            RunStepsProjection stepsProjection,
            RunLogsProjection logsProjection,
            RunAiTriageProjection aiTriageProjection,
            RunRerunProjection rerunProjection,
            Executor executor
    ) {
        this.headerProjection = headerProjection;
        this.jobsProjection = jobsProjection;
        this.stepsProjection = stepsProjection;
        this.logsProjection = logsProjection;
        this.aiTriageProjection = aiTriageProjection;
        this.rerunProjection = rerunProjection;
        this.executor = executor;
    }

    public RunDetailAggregateDto loadAggregate(String runId) {
        CompletableFuture<SectionResultDto<RunHeaderDto>> header = loadAsync(() -> loadHeader(runId), "header");
        CompletableFuture<SectionResultDto<List<RunJobRowDto>>> jobs = loadAsync(() -> loadJobs(runId), "jobs");
        CompletableFuture<SectionResultDto<List<RunStepRowDto>>> steps = loadAsync(() -> loadSteps(runId), "steps");
        CompletableFuture<SectionResultDto<String>> logs = loadAsync(() -> loadLogs(runId), "logs");
        CompletableFuture<SectionResultDto<AiTriageDto>> triage = loadAsync(() -> loadAiTriage(runId), "aiTriage");
        CompletableFuture<SectionResultDto<RunRerunDto>> rerun = loadAsync(() -> loadRerunStatus(runId), "rerun");

        CompletableFuture.allOf(header, jobs, steps, logs, triage, rerun)
                .completeOnTimeout(null, CodeBuildManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new RunDetailAggregateDto(header.join(), jobs.join(), steps.join(), logs.join(), triage.join(), rerun.join());
    }

    public RunHeaderDto loadHeader(String runId) {
        return headerProjection.load(runId);
    }

    public List<RunJobRowDto> loadJobs(String runId) {
        return jobsProjection.load(runId);
    }

    public List<RunStepRowDto> loadSteps(String runId) {
        return stepsProjection.load(runId);
    }

    public String loadLogs(String runId) {
        return logsProjection.load(runId);
    }

    public AiTriageDto loadAiTriage(String runId) {
        return aiTriageProjection.load(runId);
    }

    public RunRerunDto loadRerunStatus(String runId) {
        return rerunProjection.load(runId);
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
