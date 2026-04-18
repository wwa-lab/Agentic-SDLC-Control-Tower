package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityReqRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementTraceabilitySummaryProjection")
public class TraceabilitySummaryProjection {

    private final TraceabilityReqRowsProjection traceabilityReqRowsProjection;

    public TraceabilitySummaryProjection(TraceabilityReqRowsProjection traceabilityReqRowsProjection) {
        this.traceabilityReqRowsProjection = traceabilityReqRowsProjection;
    }

    public TraceabilitySummaryDto load(String workspaceId) {
        List<TraceabilityReqRowDto> rows = traceabilityReqRowsProjection.load(workspaceId);
        Map<CoverageStatus, Long> buckets = rows.stream()
                .collect(Collectors.groupingBy(TraceabilityReqRowDto::coverageStatus, Collectors.counting()));
        return new TraceabilitySummaryDto(workspaceId, rows.size(), buckets);
    }
}
