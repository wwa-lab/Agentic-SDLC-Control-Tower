package com.sdlctower.domain.reportcenter.definitions;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportDefinitionDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunRequestDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.TimeRangeDto;
import com.sdlctower.domain.reportcenter.policy.ScopeAuthGuard.ScopeContext;
import java.time.Instant;
import java.util.List;

public interface ReportDefinition {

    String key();

    String category();

    String name();

    String description();

    List<String> supportedScopes();

    List<String> supportedGroupings();

    String defaultGrouping();

    String chartType();

    List<DrilldownColumnSpecDto> drilldownColumns();

    ReportRunResultDto run(ExecutionContext context);

    default ReportDefinitionDto toDto() {
        return new ReportDefinitionDto(
                key(),
                category(),
                name(),
                description(),
                supportedScopes(),
                supportedGroupings(),
                defaultGrouping(),
                chartType(),
                drilldownColumns(),
                "enabled"
        );
    }

    record ExecutionContext(
            ReportRunRequestDto request,
            TimeRangeDto resolvedTimeRange,
            ScopeContext scopeContext,
            Instant snapshotAt
    ) {}
}
