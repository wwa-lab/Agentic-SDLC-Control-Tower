package com.sdlctower.domain.dashboard;

import com.sdlctower.domain.dashboard.dto.ActivityEntryDto;
import com.sdlctower.domain.dashboard.dto.AiInvolvementDto;
import com.sdlctower.domain.dashboard.dto.AiParticipationDto;
import com.sdlctower.domain.dashboard.dto.DashboardSummaryDto;
import com.sdlctower.domain.dashboard.dto.DeliveryMetricsDto;
import com.sdlctower.domain.dashboard.dto.GovernanceMetricsDto;
import com.sdlctower.domain.dashboard.dto.MetricValueDto;
import com.sdlctower.domain.dashboard.dto.QualityMetricsDto;
import com.sdlctower.domain.dashboard.dto.RecentActivityDto;
import com.sdlctower.domain.dashboard.dto.SdlcStageHealthDto;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.domain.dashboard.dto.StabilityMetricsDto;
import com.sdlctower.domain.dashboard.dto.ValueStoryDto;
import com.sdlctower.domain.dashboard.dto.ValueStoryMetricDto;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardSummaryDto getDashboardSummary() {
        return new DashboardSummaryDto(
                loadSection("sdlcHealth", this::buildSdlcHealth),
                loadSection("deliveryMetrics", this::buildDeliveryMetrics),
                loadSection("aiParticipation", this::buildAiParticipation),
                loadSection("qualityMetrics", this::buildQualityMetrics),
                loadSection("stabilityMetrics", this::buildStabilityMetrics),
                loadSection("governanceMetrics", this::buildGovernanceMetrics),
                loadSection("recentActivity", this::buildRecentActivity),
                loadSection("valueStory", this::buildValueStory)
        );
    }

    private <T> SectionResultDto<T> loadSection(String name, Supplier<T> builder) {
        try {
            return SectionResultDto.ok(builder.get());
        } catch (Exception e) {
            log.error("Failed to load dashboard section [{}]: {}", name, e.getMessage(), e);
            return SectionResultDto.fail("Failed to load " + name);
        }
    }

    private List<SdlcStageHealthDto> buildSdlcHealth() {
        return List.of(
                new SdlcStageHealthDto("requirement", "Requirement", "healthy", 24, false, "/requirements"),
                new SdlcStageHealthDto("user-story", "User Story", "healthy", 67, false, "/requirements"),
                new SdlcStageHealthDto("spec", "Spec", "warning", 12, true, "/requirements"),
                new SdlcStageHealthDto("architecture", "Architecture", "healthy", 8, false, "/design"),
                new SdlcStageHealthDto("design", "Design", "healthy", 15, false, "/design"),
                new SdlcStageHealthDto("tasks", "Tasks", "healthy", 143, false, "/project-management"),
                new SdlcStageHealthDto("code", "Code", "healthy", 89, false, "/code"),
                new SdlcStageHealthDto("test", "Test", "warning", 34, false, "/testing"),
                new SdlcStageHealthDto("deploy", "Deploy", "healthy", 7, false, "/deployment"),
                new SdlcStageHealthDto("incident", "Incident", "critical", 3, false, "/incidents"),
                new SdlcStageHealthDto("learning", "Learning", "inactive", 0, false, "/ai-center")
        );
    }

    private DeliveryMetricsDto buildDeliveryMetrics() {
        return new DeliveryMetricsDto(
                metric("Lead Time", "4.2d", "down", true),
                metric("Deploy Frequency", "3.1/wk", "up", true),
                metric("Iteration Completion", "87%", "up", true),
                "spec"
        );
    }

    private AiParticipationDto buildAiParticipation() {
        return new AiParticipationDto(
                metric("Usage Rate", "78%", "up", true),
                metric("Adoption Rate", "64%", "up", true),
                metric("Auto-Exec Success", "92%", "stable", true),
                metric("Time Saved", "124h", "up", true),
                List.of(
                        new AiInvolvementDto("requirement", true, 45),
                        new AiInvolvementDto("user-story", true, 128),
                        new AiInvolvementDto("spec", true, 342),
                        new AiInvolvementDto("architecture", true, 12),
                        new AiInvolvementDto("design", true, 56),
                        new AiInvolvementDto("tasks", true, 89),
                        new AiInvolvementDto("code", true, 567),
                        new AiInvolvementDto("test", true, 234),
                        new AiInvolvementDto("deploy", true, 45),
                        new AiInvolvementDto("incident", false, 0),
                        new AiInvolvementDto("learning", true, 23)
                )
        );
    }

    private QualityMetricsDto buildQualityMetrics() {
        return new QualityMetricsDto(
                metric("Build Success", "98.4%", "up", true),
                metric("Test Pass Rate", "99.1%", "stable", true),
                metric("Defect Density", "0.42/kloc", "down", true),
                metric("Spec Coverage", "84%", "up", true)
        );
    }

    private StabilityMetricsDto buildStabilityMetrics() {
        return new StabilityMetricsDto(
                3,
                1,
                metric("Change Failure", "2.1%", "down", true),
                metric("MTTR", "45m", "down", true),
                metric("Rollback Rate", "0.5%", "stable", true)
        );
    }

    private GovernanceMetricsDto buildGovernanceMetrics() {
        return new GovernanceMetricsDto(
                metric("Template Reuse", "82%", "up", true),
                metric("Config Drift", "3.1%", "down", true),
                metric("Audit Coverage", "91%", "up", true),
                metric("Policy Hit Rate", "97%", "stable", true)
        );
    }

    private RecentActivityDto buildRecentActivity() {
        return new RecentActivityDto(
                List.of(
                        new ActivityEntryDto("1", "Gemini Agent", "ai", "Generated Spec for Feature #452", "spec", "2026-04-16T09:55:00Z"),
                        new ActivityEntryDto("2", "Sarah Chen", "human", "Reviewed Architecture Design", "architecture", "2026-04-16T09:45:00Z"),
                        new ActivityEntryDto("3", "Codex Agent", "ai", "Implemented Component: UserAuth", "code", "2026-04-16T09:15:00Z"),
                        new ActivityEntryDto("4", "Mike Ross", "human", "Merged PR #128: Database Migration", "deploy", "2026-04-16T08:00:00Z"),
                        new ActivityEntryDto("5", "Test Agent", "ai", "Executed Regression Suite: Passed", "test", "2026-04-16T07:00:00Z"),
                        new ActivityEntryDto("6", "Gemini Agent", "ai", "Extracted User Stories from PRD", "user-story", "2026-04-16T06:00:00Z"),
                        new ActivityEntryDto("7", "Alex Kim", "human", "Opened Incident: Production Latency", "incident", "2026-04-16T04:00:00Z"),
                        new ActivityEntryDto("8", "Gemini Agent", "ai", "Analyzed Root Cause for Latency", "incident", "2026-04-16T03:20:00Z"),
                        new ActivityEntryDto("9", "Rachel Zane", "human", "Updated Requirements: API v2", "requirement", "2026-04-15T23:00:00Z"),
                        new ActivityEntryDto("10", "Codex Agent", "ai", "Optimized SQL Query in OrderService", "code", "2026-04-15T21:00:00Z")
                ),
                1542
        );
    }

    private ValueStoryDto buildValueStory() {
        return new ValueStoryDto(
                "AI agents have reduced lead time by 35% this month while maintaining 100% audit compliance.",
                List.of(
                        new ValueStoryMetricDto("Efficiency Gain", "+42%", "Increase in developer throughput vs last quarter"),
                        new ValueStoryMetricDto("Quality Lift", "-28%", "Reduction in post-release defects"),
                        new ValueStoryMetricDto("Risk Reduction", "High", "Automated policy enforcement on all specs")
                )
        );
    }

    private MetricValueDto metric(String label, String value, String trend, boolean trendIsPositive) {
        return new MetricValueDto(label, value, trend, trendIsPositive);
    }
}
