package com.sdlctower.domain.testingmanagement.service;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityReqRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.testingmanagement.policy.TestingAccessGuard;
import com.sdlctower.domain.testingmanagement.projection.TraceabilityReqRowsProjection;
import com.sdlctower.domain.testingmanagement.projection.TraceabilitySummaryProjection;
import com.sdlctower.shared.dto.SectionResultDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("testingManagementTraceabilityService")
public class TraceabilityService {

    private final TraceabilitySummaryProjection traceabilitySummaryProjection;
    private final TraceabilityReqRowsProjection traceabilityReqRowsProjection;
    private final TestingAccessGuard accessGuard;

    public TraceabilityService(
            TraceabilitySummaryProjection traceabilitySummaryProjection,
            TraceabilityReqRowsProjection traceabilityReqRowsProjection,
            TestingAccessGuard accessGuard
    ) {
        this.traceabilitySummaryProjection = traceabilitySummaryProjection;
        this.traceabilityReqRowsProjection = traceabilityReqRowsProjection;
        this.accessGuard = accessGuard;
    }

    public TraceabilityAggregateDto loadAggregate(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return new TraceabilityAggregateDto(
                SectionResultDto.ok(traceabilitySummaryProjection.load(workspaceId)),
                SectionResultDto.ok(traceabilityReqRowsProjection.load(workspaceId))
        );
    }

    public TraceabilitySummaryDto loadSummary(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return traceabilitySummaryProjection.load(workspaceId);
    }

    public List<TraceabilityReqRowDto> loadReqRows(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        return traceabilityReqRowsProjection.load(workspaceId);
    }
}
