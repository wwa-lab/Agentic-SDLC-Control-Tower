package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ChangeLogPageDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.MilestoneDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanAggregateDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanCapacityMatrixDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PlanHeaderDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ProgressNodeDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.RiskDto;
import com.sdlctower.domain.projectmanagement.projection.AiSuggestionsProjection;
import com.sdlctower.domain.projectmanagement.projection.CapacityPlanProjection;
import com.sdlctower.domain.projectmanagement.projection.DependencyGraphProjection;
import com.sdlctower.domain.projectmanagement.projection.DeliveryProgressProjection;
import com.sdlctower.domain.projectmanagement.projection.MilestonePlanProjection;
import com.sdlctower.domain.projectmanagement.projection.PlanChangeLogProjection;
import com.sdlctower.domain.projectmanagement.projection.PlanHeaderProjection;
import com.sdlctower.domain.projectmanagement.projection.RiskRegistryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanService {

    private final PlanHeaderProjection headerProjection;
    private final MilestonePlanProjection milestoneProjection;
    private final CapacityPlanProjection capacityProjection;
    private final RiskRegistryProjection riskProjection;
    private final DependencyGraphProjection dependencyProjection;
    private final DeliveryProgressProjection progressProjection;
    private final PlanChangeLogProjection changeLogProjection;
    private final AiSuggestionsProjection aiSuggestionsProjection;
    private final Executor projectionExecutor;

    @Autowired
    public PlanService(
            PlanHeaderProjection headerProjection,
            MilestonePlanProjection milestoneProjection,
            CapacityPlanProjection capacityProjection,
            RiskRegistryProjection riskProjection,
            DependencyGraphProjection dependencyProjection,
            DeliveryProgressProjection progressProjection,
            PlanChangeLogProjection changeLogProjection,
            AiSuggestionsProjection aiSuggestionsProjection
    ) {
        this(
                headerProjection,
                milestoneProjection,
                capacityProjection,
                riskProjection,
                dependencyProjection,
                progressProjection,
                changeLogProjection,
                aiSuggestionsProjection,
                ForkJoinPool.commonPool()
        );
    }

    PlanService(
            PlanHeaderProjection headerProjection,
            MilestonePlanProjection milestoneProjection,
            CapacityPlanProjection capacityProjection,
            RiskRegistryProjection riskProjection,
            DependencyGraphProjection dependencyProjection,
            DeliveryProgressProjection progressProjection,
            PlanChangeLogProjection changeLogProjection,
            AiSuggestionsProjection aiSuggestionsProjection,
            Executor projectionExecutor
    ) {
        this.headerProjection = headerProjection;
        this.milestoneProjection = milestoneProjection;
        this.capacityProjection = capacityProjection;
        this.riskProjection = riskProjection;
        this.dependencyProjection = dependencyProjection;
        this.progressProjection = progressProjection;
        this.changeLogProjection = changeLogProjection;
        this.aiSuggestionsProjection = aiSuggestionsProjection;
        this.projectionExecutor = projectionExecutor;
    }

    public PlanAggregateDto loadAggregate(String projectId) {
        CompletableFuture<SectionResultDto<PlanHeaderDto>> header = loadAsync(() -> headerProjection.load(projectId), "header");
        CompletableFuture<SectionResultDto<List<MilestoneDto>>> milestones = loadAsync(() -> milestoneProjection.load(projectId, false), "milestones");
        CompletableFuture<SectionResultDto<PlanCapacityMatrixDto>> capacity = loadAsync(() -> capacityProjection.load(projectId), "capacity");
        CompletableFuture<SectionResultDto<List<RiskDto>>> risks = loadAsync(() -> riskProjection.load(projectId, null, null), "risks");
        CompletableFuture<SectionResultDto<List<DependencyDto>>> dependencies = loadAsync(() -> dependencyProjection.load(projectId, null), "dependencies");
        CompletableFuture<SectionResultDto<List<ProgressNodeDto>>> progress = loadAsync(() -> progressProjection.load(projectId), "progress");
        CompletableFuture<SectionResultDto<ChangeLogPageDto>> changeLog = loadAsync(
                () -> changeLogProjection.load(projectId, null, null, null, null, 0, ProjectManagementConstants.DEFAULT_CHANGE_LOG_PAGE_SIZE),
                "change log"
        );
        CompletableFuture<SectionResultDto<List<AiSuggestionDto>>> aiSuggestions = loadAsync(
                () -> aiSuggestionsProjection.load(projectId, "PENDING"),
                "ai suggestions"
        );

        CompletableFuture.allOf(header, milestones, capacity, risks, dependencies, progress, changeLog, aiSuggestions)
                .completeOnTimeout(null, ProjectManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        PlanAggregateDto aggregate = new PlanAggregateDto(
                header.join(),
                milestones.join(),
                capacity.join(),
                risks.join(),
                dependencies.join(),
                progress.join(),
                changeLog.join(),
                aiSuggestions.join()
        );

        boolean allFailed = Stream.of(
                        aggregate.header(),
                        aggregate.milestones(),
                        aggregate.capacity(),
                        aggregate.risks(),
                        aggregate.dependencies(),
                        aggregate.progress(),
                        aggregate.changeLog(),
                        aggregate.aiSuggestions()
                )
                .allMatch(section -> section.error() != null);

        if (allFailed) {
            throw new IllegalStateException("Unable to load Project Management plan projections for project " + projectId);
        }

        return aggregate;
    }

    public PlanHeaderDto loadHeader(String projectId) {
        return headerProjection.load(projectId);
    }

    public List<MilestoneDto> loadMilestones(String projectId, boolean includeArchived) {
        return milestoneProjection.load(projectId, includeArchived);
    }

    public PlanCapacityMatrixDto loadCapacity(String projectId) {
        return capacityProjection.load(projectId);
    }

    public List<RiskDto> loadRisks(String projectId, String state, String severity) {
        return riskProjection.load(projectId, state, severity);
    }

    public List<DependencyDto> loadDependencies(String projectId, String state) {
        return dependencyProjection.load(projectId, state);
    }

    public List<ProgressNodeDto> loadProgress(String projectId) {
        return progressProjection.load(projectId);
    }

    public ChangeLogPageDto loadChangeLog(
            String projectId,
            String actorType,
            String targetType,
            Instant from,
            Instant to,
            Integer page,
            Integer pageSize
    ) {
        return changeLogProjection.load(projectId, actorType, targetType, from, to, page, pageSize);
    }

    public List<AiSuggestionDto> loadAiSuggestions(String projectId, String state) {
        return aiSuggestionsProjection.load(projectId, state);
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(Supplier<T> supplier, String label) {
        return CompletableFuture.supplyAsync(supplier, projectionExecutor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(capitalize(label) + " projection timed out"),
                        ProjectManagementConstants.PROJECTION_BUDGET.toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(ex -> SectionResultDto.fail(capitalize(label) + " projection failed: " + rootCause(ex).getMessage()));
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String capitalize(String label) {
        return label.substring(0, 1).toUpperCase() + label.substring(1);
    }
}
