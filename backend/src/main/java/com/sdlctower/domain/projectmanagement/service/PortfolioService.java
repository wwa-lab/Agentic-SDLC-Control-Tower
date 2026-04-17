package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.CadenceMetricDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyBottleneckDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioAggregateDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioCapacityDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioHeatmapDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioRiskConcentrationDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioSummaryDto;
import com.sdlctower.domain.projectmanagement.projection.CadenceMetricsProjection;
import com.sdlctower.domain.projectmanagement.projection.CapacityAllocationProjection;
import com.sdlctower.domain.projectmanagement.projection.DependencyBottlenecksProjection;
import com.sdlctower.domain.projectmanagement.projection.MilestoneHeatmapProjection;
import com.sdlctower.domain.projectmanagement.projection.PortfolioSummaryProjection;
import com.sdlctower.domain.projectmanagement.projection.RiskConcentrationProjection;
import com.sdlctower.shared.dto.SectionResultDto;
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
public class PortfolioService {

    private final PortfolioSummaryProjection summaryProjection;
    private final MilestoneHeatmapProjection heatmapProjection;
    private final CapacityAllocationProjection capacityProjection;
    private final RiskConcentrationProjection riskProjection;
    private final DependencyBottlenecksProjection dependencyProjection;
    private final CadenceMetricsProjection cadenceProjection;
    private final Executor projectionExecutor;

    @Autowired
    public PortfolioService(
            PortfolioSummaryProjection summaryProjection,
            MilestoneHeatmapProjection heatmapProjection,
            CapacityAllocationProjection capacityProjection,
            RiskConcentrationProjection riskProjection,
            DependencyBottlenecksProjection dependencyProjection,
            CadenceMetricsProjection cadenceProjection
    ) {
        this(
                summaryProjection,
                heatmapProjection,
                capacityProjection,
                riskProjection,
                dependencyProjection,
                cadenceProjection,
                ForkJoinPool.commonPool()
        );
    }

    PortfolioService(
            PortfolioSummaryProjection summaryProjection,
            MilestoneHeatmapProjection heatmapProjection,
            CapacityAllocationProjection capacityProjection,
            RiskConcentrationProjection riskProjection,
            DependencyBottlenecksProjection dependencyProjection,
            CadenceMetricsProjection cadenceProjection,
            Executor projectionExecutor
    ) {
        this.summaryProjection = summaryProjection;
        this.heatmapProjection = heatmapProjection;
        this.capacityProjection = capacityProjection;
        this.riskProjection = riskProjection;
        this.dependencyProjection = dependencyProjection;
        this.cadenceProjection = cadenceProjection;
        this.projectionExecutor = projectionExecutor;
    }

    public PortfolioAggregateDto loadAggregate(String workspaceId) {
        CompletableFuture<SectionResultDto<PortfolioSummaryDto>> summary = loadAsync(
                () -> summaryProjection.load(workspaceId),
                "summary"
        );
        CompletableFuture<SectionResultDto<PortfolioHeatmapDto>> heatmap = loadAsync(
                () -> heatmapProjection.load(workspaceId, "WEEK"),
                "heatmap"
        );
        CompletableFuture<SectionResultDto<PortfolioCapacityDto>> capacity = loadAsync(
                () -> capacityProjection.load(workspaceId),
                "capacity"
        );
        CompletableFuture<SectionResultDto<PortfolioRiskConcentrationDto>> risks = loadAsync(
                () -> riskProjection.load(workspaceId, 20, null, null),
                "risks"
        );
        CompletableFuture<SectionResultDto<List<DependencyBottleneckDto>>> bottlenecks = loadAsync(
                () -> dependencyProjection.load(workspaceId, 15),
                "dependencies"
        );
        CompletableFuture<SectionResultDto<List<CadenceMetricDto>>> cadence = loadAsync(
                () -> cadenceProjection.load(workspaceId),
                "cadence"
        );

        CompletableFuture.allOf(summary, heatmap, capacity, risks, bottlenecks, cadence)
                .completeOnTimeout(null, ProjectManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        PortfolioAggregateDto aggregate = new PortfolioAggregateDto(
                summary.join(),
                heatmap.join(),
                capacity.join(),
                risks.join(),
                bottlenecks.join(),
                cadence.join()
        );

        boolean allFailed = Stream.of(
                        aggregate.summary(),
                        aggregate.heatmap(),
                        aggregate.capacity(),
                        aggregate.risks(),
                        aggregate.bottlenecks(),
                        aggregate.cadence()
                )
                .allMatch(section -> section.error() != null);

        if (allFailed) {
            throw new IllegalStateException("Unable to load Project Management portfolio projections for workspace " + workspaceId);
        }

        return aggregate;
    }

    public PortfolioSummaryDto loadSummary(String workspaceId) {
        return summaryProjection.load(workspaceId);
    }

    public PortfolioHeatmapDto loadHeatmap(String workspaceId, String window) {
        return heatmapProjection.load(workspaceId, window == null ? "WEEK" : window);
    }

    public PortfolioCapacityDto loadCapacity(String workspaceId) {
        return capacityProjection.load(workspaceId);
    }

    public PortfolioRiskConcentrationDto loadRisks(String workspaceId, Integer limit, String severity, String category) {
        return riskProjection.load(workspaceId, limit, severity, category);
    }

    public List<DependencyBottleneckDto> loadDependencies(String workspaceId, Integer limit) {
        return dependencyProjection.load(workspaceId, limit);
    }

    public List<CadenceMetricDto> loadCadence(String workspaceId) {
        return cadenceProjection.load(workspaceId);
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
