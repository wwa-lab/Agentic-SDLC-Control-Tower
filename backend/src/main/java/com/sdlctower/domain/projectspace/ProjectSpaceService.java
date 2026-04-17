package com.sdlctower.domain.projectspace;

import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.DependencyMapDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.EnvironmentMatrixDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.LeadershipOwnershipDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MilestoneHubDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectSpaceAggregateDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.ProjectSummaryDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.RiskRegistryDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.SdlcChainDto;
import com.sdlctower.domain.projectspace.projection.ChainNodeProjection;
import com.sdlctower.domain.projectspace.projection.DependencyProjection;
import com.sdlctower.domain.projectspace.projection.EnvironmentMatrixProjection;
import com.sdlctower.domain.projectspace.projection.LeadershipProjection;
import com.sdlctower.domain.projectspace.projection.MilestoneProjection;
import com.sdlctower.domain.projectspace.projection.ProjectSummaryProjection;
import com.sdlctower.domain.projectspace.projection.RiskRegistryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectSpaceService {

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final ProjectSummaryProjection summaryProjection;
    private final LeadershipProjection leadershipProjection;
    private final ChainNodeProjection chainNodeProjection;
    private final MilestoneProjection milestoneProjection;
    private final DependencyProjection dependencyProjection;
    private final RiskRegistryProjection riskRegistryProjection;
    private final EnvironmentMatrixProjection environmentMatrixProjection;
    private final Executor projectionExecutor;

    @Autowired
    public ProjectSpaceService(
            ProjectSpaceSeedCatalog seedCatalog,
            ProjectSummaryProjection summaryProjection,
            LeadershipProjection leadershipProjection,
            ChainNodeProjection chainNodeProjection,
            MilestoneProjection milestoneProjection,
            DependencyProjection dependencyProjection,
            RiskRegistryProjection riskRegistryProjection,
            EnvironmentMatrixProjection environmentMatrixProjection
    ) {
        this(
                seedCatalog,
                summaryProjection,
                leadershipProjection,
                chainNodeProjection,
                milestoneProjection,
                dependencyProjection,
                riskRegistryProjection,
                environmentMatrixProjection,
                ForkJoinPool.commonPool()
        );
    }

    ProjectSpaceService(
            ProjectSpaceSeedCatalog seedCatalog,
            ProjectSummaryProjection summaryProjection,
            LeadershipProjection leadershipProjection,
            ChainNodeProjection chainNodeProjection,
            MilestoneProjection milestoneProjection,
            DependencyProjection dependencyProjection,
            RiskRegistryProjection riskRegistryProjection,
            EnvironmentMatrixProjection environmentMatrixProjection,
            Executor projectionExecutor
    ) {
        this.seedCatalog = seedCatalog;
        this.summaryProjection = summaryProjection;
        this.leadershipProjection = leadershipProjection;
        this.chainNodeProjection = chainNodeProjection;
        this.milestoneProjection = milestoneProjection;
        this.dependencyProjection = dependencyProjection;
        this.riskRegistryProjection = riskRegistryProjection;
        this.environmentMatrixProjection = environmentMatrixProjection;
        this.projectionExecutor = projectionExecutor;
    }

    public ProjectSpaceAggregateDto loadAggregate(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = ensureProjectExists(projectId);
        CompletableFuture<SectionResultDto<ProjectSummaryDto>> summary = loadAsync(projectId, summaryProjection, "summary");
        CompletableFuture<SectionResultDto<LeadershipOwnershipDto>> leadership = loadAsync(projectId, leadershipProjection, "leadership");
        CompletableFuture<SectionResultDto<SdlcChainDto>> chain = loadAsync(projectId, chainNodeProjection, "chain");
        CompletableFuture<SectionResultDto<MilestoneHubDto>> milestones = loadAsync(projectId, milestoneProjection, "milestones");
        CompletableFuture<SectionResultDto<DependencyMapDto>> dependencies = loadAsync(projectId, dependencyProjection, "dependencies");
        CompletableFuture<SectionResultDto<RiskRegistryDto>> risks = loadAsync(projectId, riskRegistryProjection, "risks");
        CompletableFuture<SectionResultDto<EnvironmentMatrixDto>> environments = loadAsync(projectId, environmentMatrixProjection, "environments");

        CompletableFuture.allOf(summary, leadership, chain, milestones, dependencies, risks, environments)
                .completeOnTimeout(null, ProjectSpaceConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        ProjectSpaceAggregateDto aggregate = new ProjectSpaceAggregateDto(
                projectId,
                project.workspaceId(),
                summary.join(),
                leadership.join(),
                chain.join(),
                milestones.join(),
                dependencies.join(),
                risks.join(),
                environments.join()
        );

        boolean allFailed = Stream.of(
                        aggregate.summary(),
                        aggregate.leadership(),
                        aggregate.chain(),
                        aggregate.milestones(),
                        aggregate.dependencies(),
                        aggregate.risks(),
                        aggregate.environments()
                )
                .allMatch(section -> section.error() != null);

        if (allFailed) {
            throw new IllegalStateException("Unable to load Project Space projections for project " + projectId);
        }

        return aggregate;
    }

    public ProjectSummaryDto loadSummary(String projectId) {
        ensureProjectExists(projectId);
        return summaryProjection.load(projectId);
    }

    public LeadershipOwnershipDto loadLeadership(String projectId) {
        ensureProjectExists(projectId);
        return leadershipProjection.load(projectId);
    }

    public SdlcChainDto loadChain(String projectId) {
        ensureProjectExists(projectId);
        return chainNodeProjection.load(projectId);
    }

    public MilestoneHubDto loadMilestones(String projectId) {
        ensureProjectExists(projectId);
        return milestoneProjection.load(projectId);
    }

    public DependencyMapDto loadDependencies(String projectId) {
        ensureProjectExists(projectId);
        return dependencyProjection.load(projectId);
    }

    public RiskRegistryDto loadRisks(String projectId) {
        ensureProjectExists(projectId);
        return riskRegistryProjection.load(projectId);
    }

    public EnvironmentMatrixDto loadEnvironments(String projectId) {
        ensureProjectExists(projectId);
        return environmentMatrixProjection.load(projectId);
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(
            String projectId,
            ProjectSpaceProjection<T> projection,
            String label
    ) {
        return CompletableFuture.supplyAsync(() -> projection.load(projectId), projectionExecutor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(capitalize(label) + " projection timed out"),
                        ProjectSpaceConstants.PROJECTION_BUDGET.toMillis(),
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

    private ProjectSpaceSeedCatalog.ProjectSeed ensureProjectExists(String projectId) {
        return seedCatalog.project(projectId);
    }
}
