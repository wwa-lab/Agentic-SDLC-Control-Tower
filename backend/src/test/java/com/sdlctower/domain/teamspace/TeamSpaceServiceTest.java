package com.sdlctower.domain.teamspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sdlctower.domain.teamspace.dto.AccountableOwnerDto;
import com.sdlctower.domain.teamspace.dto.CoverageGapDto;
import com.sdlctower.domain.teamspace.dto.FieldDto;
import com.sdlctower.domain.teamspace.dto.LineageDto;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import com.sdlctower.domain.teamspace.dto.MemberMatrixDto;
import com.sdlctower.domain.teamspace.dto.MemberMatrixRowDto;
import com.sdlctower.domain.teamspace.dto.OncallOwnerDto;
import com.sdlctower.domain.teamspace.dto.PipelineCountersDto;
import com.sdlctower.domain.teamspace.dto.ProjectCardDto;
import com.sdlctower.domain.teamspace.dto.ProjectDistributionDto;
import com.sdlctower.domain.teamspace.dto.RequirementPipelineDto;
import com.sdlctower.domain.teamspace.dto.ResponsibilityBoundaryDto;
import com.sdlctower.domain.teamspace.dto.TeamDefaultTemplatesDto;
import com.sdlctower.domain.teamspace.dto.TeamMetricItemDto;
import com.sdlctower.domain.teamspace.dto.TeamMetricsDto;
import com.sdlctower.domain.teamspace.dto.TeamOperatingModelDto;
import com.sdlctower.domain.teamspace.dto.TeamRiskRadarDto;
import com.sdlctower.domain.teamspace.dto.TeamSpaceAggregateDto;
import com.sdlctower.domain.teamspace.dto.TemplateEntryDto;
import com.sdlctower.domain.teamspace.dto.WorkspaceSummaryDto;
import com.sdlctower.domain.teamspace.projection.MemberProjection;
import com.sdlctower.domain.teamspace.projection.MetricsProjection;
import com.sdlctower.domain.teamspace.projection.OperatingModelProjection;
import com.sdlctower.domain.teamspace.projection.ProjectDistributionProjection;
import com.sdlctower.domain.teamspace.projection.RequirementPipelineProjection;
import com.sdlctower.domain.teamspace.projection.RiskRadarProjection;
import com.sdlctower.domain.teamspace.projection.TemplateInheritanceProjection;
import com.sdlctower.domain.teamspace.projection.WorkspaceProjection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TeamSpaceServiceTest {

    private ExecutorService executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void loadAggregateFansOutInParallel() {
        executor = Executors.newFixedThreadPool(8);
        TeamSpaceService service = serviceWithDelay(300, false);

        long start = System.nanoTime();
        TeamSpaceAggregateDto aggregate = service.loadAggregate("ws-default-001");
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals("ws-default-001", aggregate.workspaceId());
        assertTrue(elapsedMillis < 1_500, "Expected parallel fan-out, elapsed=" + elapsedMillis + "ms");
        assertNull(aggregate.metrics().error());
    }

    @Test
    void loadAggregateDegradesTimedOutProjection() {
        executor = Executors.newFixedThreadPool(8);
        WorkspaceProjection workspace = mock(WorkspaceProjection.class);
        OperatingModelProjection operatingModel = mock(OperatingModelProjection.class);
        MemberProjection members = mock(MemberProjection.class);
        TemplateInheritanceProjection templates = mock(TemplateInheritanceProjection.class);
        RequirementPipelineProjection pipeline = mock(RequirementPipelineProjection.class);
        MetricsProjection metrics = mock(MetricsProjection.class);
        RiskRadarProjection risks = mock(RiskRadarProjection.class);
        ProjectDistributionProjection projects = mock(ProjectDistributionProjection.class);

        when(workspace.load("ws-default-001")).thenReturn(summary());
        when(operatingModel.load("ws-default-001")).thenReturn(operatingModel());
        when(members.load("ws-default-001")).thenReturn(members());
        when(templates.load("ws-default-001")).thenReturn(templates());
        when(pipeline.load("ws-default-001")).thenReturn(pipeline());
        when(risks.load("ws-default-001")).thenReturn(risks());
        when(projects.load("ws-default-001")).thenReturn(projects());
        when(metrics.load("ws-default-001")).thenAnswer(invocation -> {
            Thread.sleep(650);
            return metrics();
        });

        TeamSpaceService service = new TeamSpaceService(
                new TeamSpaceSeedCatalog(),
                workspace,
                operatingModel,
                members,
                templates,
                pipeline,
                metrics,
                risks,
                projects,
                executor
        );

        TeamSpaceAggregateDto aggregate = service.loadAggregate("ws-default-001");

        assertNull(aggregate.metrics().data());
        assertNotNull(aggregate.metrics().error());
        assertTrue(aggregate.metrics().error().contains("timed out"));
        assertNotNull(aggregate.summary().data());
    }

    private TeamSpaceService serviceWithDelay(long delayMillis, boolean slowMetrics) {
        WorkspaceProjection workspace = mock(WorkspaceProjection.class);
        OperatingModelProjection operatingModel = mock(OperatingModelProjection.class);
        MemberProjection members = mock(MemberProjection.class);
        TemplateInheritanceProjection templates = mock(TemplateInheritanceProjection.class);
        RequirementPipelineProjection pipeline = mock(RequirementPipelineProjection.class);
        MetricsProjection metrics = mock(MetricsProjection.class);
        RiskRadarProjection risks = mock(RiskRadarProjection.class);
        ProjectDistributionProjection projects = mock(ProjectDistributionProjection.class);

        try {
            when(workspace.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, summary()));
            when(operatingModel.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, operatingModel()));
            when(members.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, members()));
            when(templates.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, templates()));
            when(pipeline.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, pipeline()));
            when(risks.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, risks()));
            when(projects.load("ws-default-001")).thenAnswer(invocation -> delayed(delayMillis, projects()));
            when(metrics.load("ws-default-001")).thenAnswer(invocation -> delayed(slowMetrics ? 650 : delayMillis, metrics()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return new TeamSpaceService(
                new TeamSpaceSeedCatalog(),
                workspace,
                operatingModel,
                members,
                templates,
                pipeline,
                metrics,
                risks,
                projects,
                executor
        );
    }

    private <T> T delayed(long delayMillis, T value) throws InterruptedException {
        Thread.sleep(delayMillis);
        return value;
    }

    private WorkspaceSummaryDto summary() {
        return new WorkspaceSummaryDto(
                "ws-default-001",
                "Global SDLC Tower",
                "app-payment-gateway-pro",
                "Payment-Gateway-Pro",
                "snow-fin-tech-ops",
                "FIN-TECH-OPS",
                7,
                4,
                "YELLOW",
                "u-007",
                "Grace Hopper",
                false,
                new ResponsibilityBoundaryDto(List.of("Payment-Gateway-Pro"), List.of("FIN-TECH-OPS"), 7)
        );
    }

    private TeamOperatingModelDto operatingModel() {
        return new TeamOperatingModelDto(
                new FieldDto<>("STANDARD", new LineageDto("APPLICATION", false, List.of())),
                new FieldDto<>("REVIEWER_REQUIRED", new LineageDto("WORKSPACE", true, List.of())),
                new FieldDto<>("HUMAN_IN_LOOP", new LineageDto("PLATFORM", false, List.of())),
                new FieldDto<>(new OncallOwnerDto("u-011", "Alan Turing", "rot-fin-tech-primary"), new LineageDto("WORKSPACE", false, List.of())),
                List.of(new AccountableOwnerDto("DELIVERY", "u-007", "Grace Hopper")),
                new LinkDto("/platform?view=config&workspaceId=ws-default-001&section=operating-model", false)
        );
    }

    private MemberMatrixDto members() {
        return new MemberMatrixDto(
                List.of(new MemberMatrixRowDto("u-007", "Grace Hopper", List.of("TEAM_LEAD"), "OFF", List.of("APPROVE"), Instant.parse("2026-04-17T09:42:00Z"))),
                List.of(new CoverageGapDto("BACKUP_MISSING", "No backup", "2026-04-19 – 2026-04-21")),
                new LinkDto("/platform?view=access&workspaceId=ws-default-001", false)
        );
    }

    private TeamDefaultTemplatesDto templates() {
        return new TeamDefaultTemplatesDto(
                Map.of("PAGE", List.of(new TemplateEntryDto("tpl-page-team-space", "Team Space Layout", "1.0.0", "PAGE", new LineageDto("PLATFORM", false, List.of()), null))),
                List.of()
        );
    }

    private RequirementPipelineDto pipeline() {
        return new RequirementPipelineDto(
                new PipelineCountersDto(12, 18, 3, 5, 2, 7),
                List.of(),
                List.of(),
                3
        );
    }

    private TeamMetricsDto metrics() {
        return new TeamMetricsDto(
                List.of(new TeamMetricItemDto("delivery.cycleTime", "Cycle Time", 4.2, 5.1, "DAYS", "DOWN", "/reports/metric/delivery.cycleTime?workspaceId=ws-default-001", "Average days from Approved to Delivered")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Instant.parse("2026-04-17T01:00:00Z")
        );
    }

    private TeamRiskRadarDto risks() {
        return new TeamRiskRadarDto(Map.of("INCIDENT", List.of()), Instant.parse("2026-04-17T10:00:00Z"), 0);
    }

    private ProjectDistributionDto projects() {
        return new ProjectDistributionDto(
                Map.of("HEALTHY", List.of(new ProjectCardDto("proj-11", "Card Issuance", "DELIVERY", "HEALTHY", null, 4, 0, "/project-space/proj-11"))),
                Map.of("HEALTHY", 1, "AT_RISK", 0, "CRITICAL", 0, "ARCHIVED", 0)
        );
    }
}
