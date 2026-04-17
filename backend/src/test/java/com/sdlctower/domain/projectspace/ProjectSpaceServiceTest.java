package com.sdlctower.domain.projectspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos;
import com.sdlctower.domain.projectspace.projection.ChainNodeProjection;
import com.sdlctower.domain.projectspace.projection.DependencyProjection;
import com.sdlctower.domain.projectspace.projection.EnvironmentMatrixProjection;
import com.sdlctower.domain.projectspace.projection.LeadershipProjection;
import com.sdlctower.domain.projectspace.projection.MilestoneProjection;
import com.sdlctower.domain.projectspace.projection.ProjectSummaryProjection;
import com.sdlctower.domain.projectspace.projection.RiskRegistryProjection;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProjectSpaceServiceTest {

    private ExecutorService executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void loadAggregateFansOutInParallel() {
        executor = Executors.newFixedThreadPool(7);
        ProjectSpaceService service = serviceWithDelay(300, false);

        long start = System.nanoTime();
        ProjectSpaceDtos.ProjectSpaceAggregateDto aggregate = service.loadAggregate("proj-42");
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals("proj-42", aggregate.projectId());
        assertTrue(elapsedMillis < 1_500, "Expected parallel fan-out, elapsed=" + elapsedMillis + "ms");
        assertNull(aggregate.environments().error());
    }

    @Test
    void loadAggregateDegradesTimedOutProjection() {
        executor = Executors.newFixedThreadPool(7);
        ProjectSummaryProjection summary = mock(ProjectSummaryProjection.class);
        LeadershipProjection leadership = mock(LeadershipProjection.class);
        ChainNodeProjection chain = mock(ChainNodeProjection.class);
        MilestoneProjection milestones = mock(MilestoneProjection.class);
        DependencyProjection dependencies = mock(DependencyProjection.class);
        RiskRegistryProjection risks = mock(RiskRegistryProjection.class);
        EnvironmentMatrixProjection environments = mock(EnvironmentMatrixProjection.class);

        when(summary.load("proj-42")).thenReturn(summary());
        when(leadership.load("proj-42")).thenReturn(leadership());
        when(chain.load("proj-42")).thenReturn(chain());
        when(milestones.load("proj-42")).thenReturn(milestones());
        when(dependencies.load("proj-42")).thenReturn(dependencies());
        when(risks.load("proj-42")).thenReturn(risks());
        when(environments.load("proj-42")).thenAnswer(invocation -> {
            Thread.sleep(650);
            return environments();
        });

        ProjectSpaceService service = new ProjectSpaceService(
                new ProjectSpaceSeedCatalog(),
                summary,
                leadership,
                chain,
                milestones,
                dependencies,
                risks,
                environments,
                executor
        );

        ProjectSpaceDtos.ProjectSpaceAggregateDto aggregate = service.loadAggregate("proj-42");

        assertNull(aggregate.environments().data());
        assertNotNull(aggregate.environments().error());
        assertTrue(aggregate.environments().error().contains("timed out"));
        assertNotNull(aggregate.summary().data());
    }

    private ProjectSpaceService serviceWithDelay(long delayMillis, boolean slowEnvironment) {
        ProjectSummaryProjection summary = mock(ProjectSummaryProjection.class);
        LeadershipProjection leadership = mock(LeadershipProjection.class);
        ChainNodeProjection chain = mock(ChainNodeProjection.class);
        MilestoneProjection milestones = mock(MilestoneProjection.class);
        DependencyProjection dependencies = mock(DependencyProjection.class);
        RiskRegistryProjection risks = mock(RiskRegistryProjection.class);
        EnvironmentMatrixProjection environments = mock(EnvironmentMatrixProjection.class);

        try {
            when(summary.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, summary()));
            when(leadership.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, leadership()));
            when(chain.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, chain()));
            when(milestones.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, milestones()));
            when(dependencies.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, dependencies()));
            when(risks.load("proj-42")).thenAnswer(invocation -> delayed(delayMillis, risks()));
            when(environments.load("proj-42")).thenAnswer(invocation -> delayed(slowEnvironment ? 650 : delayMillis, environments()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return new ProjectSpaceService(
                new ProjectSpaceSeedCatalog(),
                summary,
                leadership,
                chain,
                milestones,
                dependencies,
                risks,
                environments,
                executor
        );
    }

    private <T> T delayed(long delayMillis, T value) throws InterruptedException {
        Thread.sleep(delayMillis);
        return value;
    }

    private ProjectSpaceDtos.ProjectSummaryDto summary() {
        return new ProjectSpaceDtos.ProjectSummaryDto(
                "proj-42",
                "Gateway Migration",
                "ws-default-001",
                "Global SDLC Tower",
                "app-payment-gateway-pro",
                "Payment-Gateway-Pro",
                "DELIVERY",
                "YELLOW",
                List.of(new ProjectSpaceDtos.HealthFactorDto("Delivery risk", "WARN")),
                new ProjectSpaceDtos.MemberRefDto("u-007", "Grace Hopper"),
                new ProjectSpaceDtos.MemberRefDto("u-011", "Alan Turing"),
                new ProjectSpaceDtos.MilestoneRefDto("MS-PROJ42-02", "Alpha Release", java.time.LocalDate.parse("2026-05-01")),
                new ProjectSpaceDtos.ProjectCountersDto(7, 1, 3, 2),
                Instant.parse("2026-04-17T10:05:00Z"),
                "/team?workspaceId=ws-default-001"
        );
    }

    private ProjectSpaceDtos.LeadershipOwnershipDto leadership() {
        return new ProjectSpaceDtos.LeadershipOwnershipDto(
                List.of(new ProjectSpaceDtos.RoleAssignmentDto("PM", "u-007", "Grace Hopper", "OFF", true, "u-012", "Katherine Johnson")),
                new com.sdlctower.domain.teamspace.dto.LinkDto("/platform?view=access&projectId=proj-42", false)
        );
    }

    private ProjectSpaceDtos.SdlcChainDto chain() {
        return new ProjectSpaceDtos.SdlcChainDto(
                List.of(new ProjectSpaceDtos.ChainNodeHealthDto("REQUIREMENT", "Requirement", 12, "GREEN", false, "/requirements?projectId=proj-42", true))
        );
    }

    private ProjectSpaceDtos.MilestoneHubDto milestones() {
        return new ProjectSpaceDtos.MilestoneHubDto(
                List.of(new ProjectSpaceDtos.MilestoneDto("MS-PROJ42-02", "Alpha Release", java.time.LocalDate.parse("2026-05-01"), "IN_PROGRESS", 60, new ProjectSpaceDtos.MemberRefDto("u-007", "Grace Hopper"), true, null)),
                new com.sdlctower.domain.teamspace.dto.LinkDto("/project-management?projectId=proj-42", false)
        );
    }

    private ProjectSpaceDtos.DependencyMapDto dependencies() {
        return new ProjectSpaceDtos.DependencyMapDto(
                List.of(new ProjectSpaceDtos.DependencyDto("DEP-1", "Identity-Service-V2", "proj-identity-v2", null, true, "UPSTREAM", "API", "Identity Platform", "YELLOW", "Token exchange is slipping", null)),
                List.of()
        );
    }

    private ProjectSpaceDtos.RiskRegistryDto risks() {
        return new ProjectSpaceDtos.RiskRegistryDto(
                List.of(new ProjectSpaceDtos.RiskItemDto("PRISK-1", "Dependency risk", "HIGH", "DEPENDENCY", new ProjectSpaceDtos.MemberRefDto("u-011", "Alan Turing"), 4, "Partner team moved completion", new com.sdlctower.domain.teamspace.dto.LinkDto("/project-space/proj-42", true), null)),
                1,
                Instant.parse("2026-04-17T10:05:00Z")
        );
    }

    private ProjectSpaceDtos.EnvironmentMatrixDto environments() {
        return new ProjectSpaceDtos.EnvironmentMatrixDto(
                List.of(new ProjectSpaceDtos.EnvironmentDto("ENV-P42-STAGE", "STAGING", "STAGING", "v2.4.0-rc.4", "build-1035", "GREEN", "APPROVAL_REQUIRED", new ProjectSpaceDtos.MemberRefDto("u-003", "Ada Lovelace"), Instant.parse("2026-04-17T06:45:00Z"), new ProjectSpaceDtos.VersionDriftDto("MINOR", 6, "v2.4.0", "STAGING trails PROD by 6 commits"), new com.sdlctower.domain.teamspace.dto.LinkDto("/deployment?projectId=proj-42&envId=ENV-P42-STAGE", false)))
        );
    }
}
