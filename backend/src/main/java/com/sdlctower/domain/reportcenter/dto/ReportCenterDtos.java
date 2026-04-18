package com.sdlctower.domain.reportcenter.dto;

import com.sdlctower.shared.dto.SectionResultDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ReportCenterDtos {

    private ReportCenterDtos() {}

    public record DrilldownColumnSpecDto(
            String key,
            String label,
            String type,
            String format
    ) {}

    public record ReportDefinitionDto(
            String reportKey,
            String category,
            String name,
            String description,
            List<String> supportedScopes,
            List<String> supportedGroupings,
            String defaultGrouping,
            String chartType,
            List<DrilldownColumnSpecDto> drilldownColumns,
            String status
    ) {}

    public record CatalogCategoryGroupDto(
            String category,
            String label,
            List<ReportDefinitionDto> reports
    ) {}

    public record CatalogDto(
            List<CatalogCategoryGroupDto> categories
    ) {}

    public record TimeRangeDto(
            @NotBlank String preset,
            Instant startAt,
            Instant endAt
    ) {}

    public record ReportRunRequestDto(
            @NotBlank String scope,
            @NotEmpty @Size(max = 20) List<@NotBlank String> scopeIds,
            @NotNull @Valid TimeRangeDto timeRange,
            @NotBlank String grouping,
            Map<String, Object> extraFilters
    ) {}

    public record HeadlineMetricDto(
            String key,
            String label,
            String value,
            double numericValue,
            Double trend,
            Boolean trendIsPositive
    ) {}

    public record SeriesPointDto(
            String groupKey,
            String groupLabel,
            Object x,
            double y,
            Map<String, Double> secondary
    ) {}

    public record DrilldownDto(
            List<DrilldownColumnSpecDto> columns,
            List<Map<String, Object>> rows,
            int totalRows
    ) {}

    public record ReportRunResultDto(
            String reportKey,
            Instant snapshotAt,
            String scope,
            List<String> scopeIds,
            TimeRangeDto timeRange,
            String grouping,
            SectionResultDto<List<HeadlineMetricDto>> headline,
            SectionResultDto<List<SeriesPointDto>> series,
            SectionResultDto<DrilldownDto> drilldown,
            Boolean slow
    ) {}

    public record ExportJobDto(
            String id,
            String reportKey,
            String format,
            String status,
            String downloadUrl,
            String errorMessage,
            Long bytes,
            Instant createdAt,
            Instant readyAt,
            Instant expiresAt
    ) {}

    public record ReportRunHistoryEntryDto(
            String runId,
            String reportKey,
            String reportName,
            String scope,
            String scopeSummary,
            String timeRangeLabel,
            String grouping,
            Instant runAt,
            long durationMs
    ) {}

    public record ReportExportHistoryEntryDto(
            String exportId,
            String reportKey,
            String reportName,
            String format,
            String status,
            Instant createdAt,
            Instant expiresAt,
            String downloadUrl,
            Long bytes
    ) {}
}
