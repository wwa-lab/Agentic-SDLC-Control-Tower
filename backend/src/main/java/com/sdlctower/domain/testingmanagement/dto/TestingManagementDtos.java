package com.sdlctower.domain.testingmanagement.dto;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.DraftOrigin;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.ReqLinkStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.RunTriggerSource;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCasePriority;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseState;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseType;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestEnvironmentKind;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestPlanState;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestRunState;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class TestingManagementDtos {

    private TestingManagementDtos() {}

    public record MemberRefDto(String memberId, String displayName) {}

    public record RequirementChipDto(
            String reqId,
            String storyId,
            String title,
            String projectId,
            ReqLinkStatus linkStatus,
            CoverageStatus chipColor,
            String routePath
    ) {}

    public record IncidentChipDto(
            String incidentId,
            String title,
            String severity,
            String routePath
    ) {}

    public record CatalogFiltersDto(
            List<String> projectIds,
            List<TestPlanState> planStates,
            List<CoverageStatus> coverageStatuses
    ) {}

    public record CatalogSummaryDto(
            String workspaceId,
            int totalPlans,
            int totalActiveCases,
            int runsLast7d,
            double passRateLast7d,
            double meanRunDurationSec,
            Map<CoverageStatus, Long> byCoverageLed
    ) {}

    public record CatalogPlanRowDto(
            String planId,
            String projectId,
            String projectName,
            String workspaceId,
            String name,
            String releaseTarget,
            MemberRefDto owner,
            TestPlanState state,
            int linkedCaseCount,
            CoverageStatus coverageLed,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CatalogAggregateDto(
            SectionResultDto<CatalogSummaryDto> summary,
            SectionResultDto<List<CatalogPlanRowDto>> grid,
            SectionResultDto<CatalogFiltersDto> filters
    ) {}

    public record PlanHeaderDto(
            String planId,
            String projectId,
            String projectName,
            String workspaceId,
            String workspaceName,
            String name,
            String description,
            String releaseTarget,
            MemberRefDto owner,
            TestPlanState state,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record PlanCaseRowDto(
            String caseId,
            String planId,
            String title,
            TestCaseType type,
            TestCasePriority priority,
            TestCaseState state,
            List<RequirementChipDto> linkedReqs,
            TestResultOutcome lastRunStatus,
            Instant lastRunAt
    ) {}

    public record CoverageRowDto(
            String reqId,
            String reqTitle,
            int linkedCaseCount,
            CoverageStatus aggregateStatus,
            Instant mostRecentAt
    ) {}

    public record RecentRunRowDto(
            String runId,
            String environmentId,
            String environmentName,
            RunTriggerSource triggerSource,
            TestRunState state,
            Integer durationSec,
            int passCount,
            int failCount,
            int skipCount,
            MemberRefDto actor,
            Instant startedAt,
            Instant completedAt
    ) {}

    public record AiDraftRowDto(
            String caseId,
            String title,
            String sourceReqId,
            String skillVersion,
            Instant draftedAt,
            boolean draft
    ) {}

    public record AiInsightsDto(
            String autonomyLevel,
            int pendingDrafts,
            int activeCases,
            double passRateLast7d,
            String narrative
    ) {}

    public record PlanDetailAggregateDto(
            SectionResultDto<PlanHeaderDto> header,
            SectionResultDto<List<PlanCaseRowDto>> cases,
            SectionResultDto<List<CoverageRowDto>> coverage,
            SectionResultDto<List<RecentRunRowDto>> recentRuns,
            SectionResultDto<List<AiDraftRowDto>> draftInbox,
            SectionResultDto<AiInsightsDto> aiInsights
    ) {}

    public record CaseDetailDto(
            String caseId,
            String planId,
            String projectId,
            String planName,
            String title,
            TestCaseType type,
            TestCasePriority priority,
            TestCaseState state,
            DraftOrigin origin,
            MemberRefDto owner,
            String preconditions,
            String steps,
            String expectedResult,
            List<RequirementChipDto> linkedReqs,
            List<IncidentChipDto> linkedIncidents,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CaseRunOutcomeDto(
            String resultId,
            String runId,
            TestResultOutcome outcome,
            String failureExcerpt,
            Instant lastPassedAt,
            String environmentName,
            Instant createdAt
    ) {}

    public record CaseRevisionDto(
            String revisionId,
            MemberRefDto actor,
            Instant timestamp,
            Map<String, String> fieldDiff
    ) {}

    public record CaseDetailAggregateDto(
            SectionResultDto<CaseDetailDto> detail,
            SectionResultDto<List<CaseRunOutcomeDto>> recentResults,
            SectionResultDto<List<CaseRevisionDto>> revisions
    ) {}

    public record RunHeaderDto(
            String runId,
            String planId,
            String projectId,
            String planName,
            String environmentId,
            String environmentName,
            TestEnvironmentKind environmentKind,
            RunTriggerSource triggerSource,
            MemberRefDto actor,
            TestRunState state,
            String externalRunId,
            Integer durationSec,
            Instant startedAt,
            Instant completedAt
    ) {}

    public record RunCaseResultRowDto(
            String resultId,
            String caseId,
            String title,
            TestResultOutcome outcome,
            Integer durationSec,
            String failureExcerpt,
            Instant lastPassedAt,
            Instant createdAt
    ) {}

    public record RunCoverageDto(
            int coveredRequirementCount,
            List<RequirementChipDto> coveredRequirements
    ) {}

    public record RunDetailAggregateDto(
            SectionResultDto<RunHeaderDto> header,
            SectionResultDto<List<RunCaseResultRowDto>> caseResults,
            SectionResultDto<RunCoverageDto> coverage
    ) {}

    public record TraceabilityReqRowDto(
            String reqId,
            String reqTitle,
            String storyId,
            String projectId,
            String projectName,
            int linkedCaseCount,
            int linkedPlanCount,
            CoverageStatus coverageStatus,
            Instant latestRunAt,
            List<PlanCaseRefDto> cases
    ) {}

    public record PlanCaseRefDto(
            String caseId,
            String title,
            String planId,
            String planName,
            TestResultOutcome lastRunStatus,
            Instant lastRunAt
    ) {}

    public record TraceabilitySummaryDto(
            String workspaceId,
            int totalRequirements,
            Map<CoverageStatus, Long> buckets
    ) {}

    public record TraceabilityAggregateDto(
            SectionResultDto<TraceabilitySummaryDto> summary,
            SectionResultDto<List<TraceabilityReqRowDto>> reqRows
    ) {}
}
