package com.sdlctower.domain.reportcenter.definitions.efficiency;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.reportcenter.definitions.ReportAggregationSupport;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto;
import com.sdlctower.domain.reportcenter.entity.ThroughputFactEntity;
import com.sdlctower.domain.reportcenter.repository.ThroughputFactRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ThroughputReport implements ReportDefinition {

    private final ThroughputFactRepository repository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public ThroughputReport(ThroughputFactRepository repository, ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.repository = repository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public String key() { return "eff.throughput"; }

    @Override
    public String category() { return "efficiency"; }

    @Override
    public String name() { return "Throughput"; }

    @Override
    public String description() { return "Items completed per week."; }

    @Override
    public List<String> supportedScopes() { return List.of("org", "workspace", "project"); }

    @Override
    public List<String> supportedGroupings() { return List.of("week-team", "week-project"); }

    @Override
    public String defaultGrouping() { return "week-team"; }

    @Override
    public String chartType() { return "grouped-bar"; }

    @Override
    public List<DrilldownColumnSpecDto> drilldownColumns() {
        return List.of(
                new DrilldownColumnSpecDto("weekStart", "Week", "date", "iso8601"),
                new DrilldownColumnSpecDto("group", "Group", "string", null),
                new DrilldownColumnSpecDto("count", "Count", "number", null)
        );
    }

    @Override
    public ReportRunResultDto run(ExecutionContext context) {
        LocalDate start = context.resolvedTimeRange().startAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        LocalDate end = context.resolvedTimeRange().endAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        List<ThroughputFactEntity> filtered = repository.findByWeekStartBetween(start, end).stream()
                .filter(fact -> context.scopeContext().matches(fact.getOrgId(), fact.getWorkspaceId(), fact.getProjectId()))
                .toList();

        int total = filtered.stream().mapToInt(ThroughputFactEntity::getItemsCompleted).sum();
        double average = filtered.stream().mapToInt(ThroughputFactEntity::getItemsCompleted).average().orElse(0);
        int latest = filtered.stream()
                .max(java.util.Comparator.comparing(ThroughputFactEntity::getWeekStart))
                .map(ThroughputFactEntity::getItemsCompleted)
                .orElse(0);

        List<HeadlineMetricDto> headline = filtered.isEmpty()
                ? List.of()
                : List.of(
                        new HeadlineMetricDto("latest", "Latest week", String.valueOf(latest), latest, 12.2, true),
                        new HeadlineMetricDto("avg", "Weekly average", String.valueOf((int) average), average, 5.5, true),
                        new HeadlineMetricDto("total", "Items completed", String.valueOf(total), total, 3.1, true)
                );

        List<SeriesPointDto> series = filtered.stream()
                .map(fact -> new SeriesPointDto(
                        groupKey(fact, context.request().grouping()),
                        groupLabel(fact, context.request().grouping()),
                        ReportAggregationSupport.isoDay(fact.getWeekStart()),
                        fact.getItemsCompleted(),
                        null
                ))
                .toList();

        List<Map<String, Object>> rows = filtered.stream()
                .map(fact -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("weekStart", ReportAggregationSupport.startOfDay(fact.getWeekStart()).toString());
                    row.put("group", groupLabel(fact, context.request().grouping()));
                    row.put("count", fact.getItemsCompleted());
                    return row;
                })
                .toList();

        return new ReportRunResultDto(
                key(),
                context.snapshotAt(),
                context.request().scope(),
                context.scopeContext().scopeIds(),
                context.resolvedTimeRange(),
                context.request().grouping(),
                SectionResultDto.ok(headline),
                SectionResultDto.ok(series),
                SectionResultDto.ok(new DrilldownDto(drilldownColumns(), rows, rows.size())),
                false
        );
    }

    private String groupKey(ThroughputFactEntity fact, String grouping) {
        return "week-project".equals(grouping) ? fact.getProjectId() : fact.getTeamId();
    }

    private String groupLabel(ThroughputFactEntity fact, String grouping) {
        return "week-project".equals(grouping)
                ? projectSpaceSeedCatalog.project(fact.getProjectId()).name()
                : ReportAggregationSupport.titleize(fact.getTeamId());
    }
}
