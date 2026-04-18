package com.sdlctower.domain.reportcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.reportcenter.config.ReportCenterProperties;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinitionRegistry;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunRequestDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.TimeRangeDto;
import com.sdlctower.domain.reportcenter.entity.ReportRun;
import com.sdlctower.domain.reportcenter.policy.ScopeAuthGuard;
import com.sdlctower.domain.reportcenter.repository.ReportRunRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportRunService {

    private final ReportDefinitionRegistry registry;
    private final ScopeAuthGuard scopeAuthGuard;
    private final ReportRunRepository reportRunRepository;
    private final ReportCenterProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();

    public ReportRunService(
            ReportDefinitionRegistry registry,
            ScopeAuthGuard scopeAuthGuard,
            ReportRunRepository reportRunRepository,
            ReportCenterProperties properties,
            ObjectMapper objectMapper
    ) {
        this.registry = registry;
        this.scopeAuthGuard = scopeAuthGuard;
        this.reportRunRepository = reportRunRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RunExecution run(String reportKey, ReportRunRequestDto request) {
        ReportDefinition definition = registry.require(reportKey);
        validateRequest(definition, request);

        ScopeAuthGuard.ScopeContext scopeContext = scopeAuthGuard.authorize(request.scope(), request.scopeIds(), properties.orgId());
        TimeRangeDto resolvedTimeRange = resolveTimeRange(request.timeRange());
        Instant start = clock.instant();
        Instant snapshotAt = clock.instant();
        ReportRunResultDto rawResult = definition.run(new ReportDefinition.ExecutionContext(request, resolvedTimeRange, scopeContext, snapshotAt));
        int durationMs = Math.toIntExact(Duration.between(start, clock.instant()).toMillis());
        ReportRunResultDto result = applySectionErrorOverrides(rawResult, request.extraFilters(), durationMs > 2500);
        int rowCount = result.drilldown().data() == null ? 0 : result.drilldown().data().rows().size();

        ReportRun entity = ReportRun.create(
                UUID.randomUUID().toString(),
                reportKey,
                scopeContext.actorId(),
                request.scope(),
                toJson(scopeContext.scopeIds()),
                resolvedTimeRange.preset(),
                resolvedTimeRange.startAt(),
                resolvedTimeRange.endAt(),
                request.grouping(),
                toJson(request.extraFilters() == null ? Map.of() : request.extraFilters()),
                snapshotAt,
                durationMs,
                rowCount
        );
        reportRunRepository.save(entity);
        return new RunExecution(result, entity, durationMs);
    }

    public ReportRunRequestDto restoreRequest(ReportRun reportRun) {
        return new ReportRunRequestDto(
                reportRun.getScope(),
                readList(reportRun.getScopeIdsJson()),
                new TimeRangeDto(reportRun.getTimeRangePreset(), reportRun.getTimeRangeStart(), reportRun.getTimeRangeEnd()),
                reportRun.getGrouping(),
                readMap(reportRun.getExtraFiltersJson())
        );
    }

    private void validateRequest(ReportDefinition definition, ReportRunRequestDto request) {
        if (!definition.supportedScopes().contains(request.scope())) {
            throw new IllegalArgumentException("grouping request scope is not supported for reportKey=" + definition.key());
        }
        if (!definition.supportedGroupings().contains(request.grouping())) {
            throw new IllegalArgumentException("grouping \"" + request.grouping() + "\" is not supported for reportKey=" + definition.key());
        }
        if ("org".equals(request.scope()) && request.scopeIds().size() != 1) {
            throw new IllegalArgumentException("Org scope requires exactly one scope id");
        }
        if ("custom".equals(request.timeRange().preset())) {
            if (request.timeRange().startAt() == null || request.timeRange().endAt() == null) {
                throw new IllegalArgumentException("Custom time range requires both startAt and endAt");
            }
            if (!request.timeRange().startAt().isBefore(request.timeRange().endAt())) {
                throw new IllegalArgumentException("Custom time range startAt must be before endAt");
            }
            if (Duration.between(request.timeRange().startAt(), request.timeRange().endAt()).toDays() > 366) {
                throw new IllegalArgumentException("Custom time range cannot exceed 366 days");
            }
        }
    }

    private TimeRangeDto resolveTimeRange(TimeRangeDto timeRange) {
        Instant now = clock.instant();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        return switch (timeRange.preset()) {
            case "last7d" -> new TimeRangeDto("last7d", today.minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC), today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            case "last30d" -> new TimeRangeDto("last30d", today.minusDays(30).atStartOfDay().toInstant(ZoneOffset.UTC), today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            case "last90d" -> new TimeRangeDto("last90d", today.minusDays(90).atStartOfDay().toInstant(ZoneOffset.UTC), today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            case "qtd" -> new TimeRangeDto("qtd", today.with(today.getMonth().firstMonthOfQuarter()).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC), today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            case "ytd" -> new TimeRangeDto("ytd", today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay().toInstant(ZoneOffset.UTC), today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
            case "custom" -> new TimeRangeDto("custom", timeRange.startAt(), timeRange.endAt());
            default -> throw new IllegalArgumentException("Unsupported time range preset: " + timeRange.preset());
        };
    }

    private ReportRunResultDto applySectionErrorOverrides(ReportRunResultDto result, Map<String, Object> extraFilters, boolean slow) {
        String mockError = extraFilters == null ? null : String.valueOf(extraFilters.getOrDefault("mockSectionError", ""));
        SectionResultDto<List<com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto>> headline = result.headline();
        SectionResultDto<List<com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto>> series = result.series();
        SectionResultDto<DrilldownDto> drilldown = result.drilldown();
        if ("headline".equalsIgnoreCase(mockError)) {
            headline = SectionResultDto.fail("Mock headline failure");
        }
        if ("series".equalsIgnoreCase(mockError)) {
            series = SectionResultDto.fail("Mock series failure");
        }
        if ("drilldown".equalsIgnoreCase(mockError)) {
            drilldown = SectionResultDto.fail("Mock drilldown failure");
        }
        return new ReportRunResultDto(
                result.reportKey(),
                result.snapshotAt(),
                result.scope(),
                result.scopeIds(),
                result.timeRange(),
                result.grouping(),
                headline,
                series,
                drilldown,
                slow
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize report state", ex);
        }
    }

    private List<String> readList(String value) {
        try {
            return value == null || value.isBlank()
                    ? List.of()
                    : objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize scope ids", ex);
        }
    }

    private Map<String, Object> readMap(String value) {
        try {
            return value == null || value.isBlank()
                    ? Map.of()
                    : objectMapper.readValue(value, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize filters", ex);
        }
    }

    public record RunExecution(
            ReportRunResultDto result,
            ReportRun entity,
            int durationMs
    ) {}
}
