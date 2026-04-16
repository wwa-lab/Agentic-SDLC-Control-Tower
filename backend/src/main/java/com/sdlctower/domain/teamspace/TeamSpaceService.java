package com.sdlctower.domain.teamspace;

import com.sdlctower.domain.teamspace.dto.MemberMatrixDto;
import com.sdlctower.domain.teamspace.dto.ProjectDistributionDto;
import com.sdlctower.domain.teamspace.dto.RequirementPipelineDto;
import com.sdlctower.domain.teamspace.dto.TeamDefaultTemplatesDto;
import com.sdlctower.domain.teamspace.dto.TeamMetricsDto;
import com.sdlctower.domain.teamspace.dto.TeamOperatingModelDto;
import com.sdlctower.domain.teamspace.dto.TeamRiskRadarDto;
import com.sdlctower.domain.teamspace.dto.TeamSpaceAggregateDto;
import com.sdlctower.domain.teamspace.dto.WorkspaceSummaryDto;
import com.sdlctower.domain.teamspace.projection.MemberProjection;
import com.sdlctower.domain.teamspace.projection.MetricsProjection;
import com.sdlctower.domain.teamspace.projection.OperatingModelProjection;
import com.sdlctower.domain.teamspace.projection.ProjectDistributionProjection;
import com.sdlctower.domain.teamspace.projection.RequirementPipelineProjection;
import com.sdlctower.domain.teamspace.projection.RiskRadarProjection;
import com.sdlctower.domain.teamspace.projection.TemplateInheritanceProjection;
import com.sdlctower.domain.teamspace.projection.WorkspaceProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamSpaceService {

    private final TeamSpaceSeedCatalog seedCatalog;
    private final WorkspaceProjection workspaceProjection;
    private final OperatingModelProjection operatingModelProjection;
    private final MemberProjection memberProjection;
    private final TemplateInheritanceProjection templateProjection;
    private final RequirementPipelineProjection pipelineProjection;
    private final MetricsProjection metricsProjection;
    private final RiskRadarProjection riskProjection;
    private final ProjectDistributionProjection projectProjection;
    private final Executor projectionExecutor;

    @Autowired
    public TeamSpaceService(
            TeamSpaceSeedCatalog seedCatalog,
            WorkspaceProjection workspaceProjection,
            OperatingModelProjection operatingModelProjection,
            MemberProjection memberProjection,
            TemplateInheritanceProjection templateProjection,
            RequirementPipelineProjection pipelineProjection,
            MetricsProjection metricsProjection,
            RiskRadarProjection riskProjection,
            ProjectDistributionProjection projectProjection
    ) {
        this(
                seedCatalog,
                workspaceProjection,
                operatingModelProjection,
                memberProjection,
                templateProjection,
                pipelineProjection,
                metricsProjection,
                riskProjection,
                projectProjection,
                ForkJoinPool.commonPool()
        );
    }

    TeamSpaceService(
            TeamSpaceSeedCatalog seedCatalog,
            WorkspaceProjection workspaceProjection,
            OperatingModelProjection operatingModelProjection,
            MemberProjection memberProjection,
            TemplateInheritanceProjection templateProjection,
            RequirementPipelineProjection pipelineProjection,
            MetricsProjection metricsProjection,
            RiskRadarProjection riskProjection,
            ProjectDistributionProjection projectProjection,
            Executor projectionExecutor
    ) {
        this.seedCatalog = seedCatalog;
        this.workspaceProjection = workspaceProjection;
        this.operatingModelProjection = operatingModelProjection;
        this.memberProjection = memberProjection;
        this.templateProjection = templateProjection;
        this.pipelineProjection = pipelineProjection;
        this.metricsProjection = metricsProjection;
        this.riskProjection = riskProjection;
        this.projectProjection = projectProjection;
        this.projectionExecutor = projectionExecutor;
    }

    public TeamSpaceAggregateDto loadAggregate(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        CompletableFuture<SectionResultDto<WorkspaceSummaryDto>> summary = loadAsync(workspaceId, workspaceProjection, "summary");
        CompletableFuture<SectionResultDto<TeamOperatingModelDto>> operatingModel = loadAsync(workspaceId, operatingModelProjection, "operating model");
        CompletableFuture<SectionResultDto<MemberMatrixDto>> members = loadAsync(workspaceId, memberProjection, "members");
        CompletableFuture<SectionResultDto<TeamDefaultTemplatesDto>> templates = loadAsync(workspaceId, templateProjection, "templates");
        CompletableFuture<SectionResultDto<RequirementPipelineDto>> pipeline = loadAsync(workspaceId, pipelineProjection, "pipeline");
        CompletableFuture<SectionResultDto<TeamMetricsDto>> metrics = loadAsync(workspaceId, metricsProjection, "metrics");
        CompletableFuture<SectionResultDto<TeamRiskRadarDto>> risks = loadAsync(workspaceId, riskProjection, "risks");
        CompletableFuture<SectionResultDto<ProjectDistributionDto>> projects = loadAsync(workspaceId, projectProjection, "projects");

        CompletableFuture.allOf(summary, operatingModel, members, templates, pipeline, metrics, risks, projects)
                .completeOnTimeout(null, TeamSpaceConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        TeamSpaceAggregateDto aggregate = new TeamSpaceAggregateDto(
                workspaceId,
                summary.join(),
                operatingModel.join(),
                members.join(),
                templates.join(),
                pipeline.join(),
                metrics.join(),
                risks.join(),
                projects.join()
        );

        boolean allFailed = Stream.of(
                        aggregate.summary(),
                        aggregate.operatingModel(),
                        aggregate.members(),
                        aggregate.templates(),
                        aggregate.pipeline(),
                        aggregate.metrics(),
                        aggregate.risks(),
                        aggregate.projects()
                )
                .allMatch(section -> section.error() != null);

        if (allFailed) {
            throw new IllegalStateException("Unable to load Team Space projections for workspace " + workspaceId);
        }

        return aggregate;
    }

    public WorkspaceSummaryDto loadSummary(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return workspaceProjection.load(workspaceId);
    }

    public TeamOperatingModelDto loadOperatingModel(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return operatingModelProjection.load(workspaceId);
    }

    public MemberMatrixDto loadMembers(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return memberProjection.load(workspaceId);
    }

    public TeamDefaultTemplatesDto loadTemplates(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return templateProjection.load(workspaceId);
    }

    public RequirementPipelineDto loadPipeline(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return pipelineProjection.load(workspaceId);
    }

    public TeamMetricsDto loadMetrics(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return metricsProjection.load(workspaceId);
    }

    public TeamRiskRadarDto loadRisks(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return riskProjection.load(workspaceId);
    }

    public ProjectDistributionDto loadProjects(String workspaceId) {
        ensureWorkspaceExists(workspaceId);
        return projectProjection.load(workspaceId);
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(
            String workspaceId,
            TeamSpaceProjection<T> projection,
            String label
    ) {
        return CompletableFuture.supplyAsync(() -> projection.load(workspaceId), projectionExecutor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(capitalize(label) + " projection timed out"),
                        TeamSpaceConstants.PROJECTION_BUDGET.toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(ex -> SectionResultDto.fail(toMessage(label, ex)));
    }

    private String toMessage(String label, Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return capitalize(label) + " projection failed: " + cause.getMessage();
    }

    private String capitalize(String label) {
        return label.substring(0, 1).toUpperCase() + label.substring(1);
    }

    private void ensureWorkspaceExists(String workspaceId) {
        seedCatalog.workspace(workspaceId);
    }
}
