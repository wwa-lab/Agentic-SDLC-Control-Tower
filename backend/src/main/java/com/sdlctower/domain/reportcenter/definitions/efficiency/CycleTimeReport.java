package com.sdlctower.domain.reportcenter.definitions.efficiency;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.reportcenter.definitions.ReportAggregationSupport;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto;
import com.sdlctower.domain.reportcenter.entity.CycleTimeFactEntity;
import com.sdlctower.domain.reportcenter.repository.CycleTimeFactRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CycleTimeReport implements ReportDefinition {

    private final CycleTimeFactRepository repository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public CycleTimeReport(CycleTimeFactRepository repository, ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.repository = repository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public String key() { return "eff.cycle-time"; }

    @Override
    public String category() { return "efficiency"; }

    @Override
    public String name() { return "Cycle Time by Stage"; }

    @Override
    public String description() { return "Time in each SDLC stage."; }

    @Override
    public List<String> supportedScopes() { return List.of("org", "workspace", "project"); }

    @Override
    public List<String> supportedGroupings() { return List.of("stage", "team", "project"); }

    @Override
    public String defaultGrouping() { return "stage"; }

    @Override
    public String chartType() { return "stacked-bar"; }

    @Override
    public List<DrilldownColumnSpecDto> drilldownColumns() {
        return List.of(
                new DrilldownColumnSpecDto("stage", "Stage", "string", null),
                new DrilldownColumnSpecDto("team", "Team", "string", null),
                new DrilldownColumnSpecDto("avgMin", "Avg (min)", "duration", "minutes")
        );
    }

    @Override
    public ReportRunResultDto run(ExecutionContext context) {
        LocalDate start = context.resolvedTimeRange().startAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        LocalDate end = context.resolvedTimeRange().endAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        List<CycleTimeFactEntity> filtered = repository.findByBucketDateBetween(start, end).stream()
                .filter(fact -> context.scopeContext().matches(fact.getOrgId(), fact.getWorkspaceId(), fact.getProjectId()))
                .toList();

        Map<String, List<CycleTimeFactEntity>> byStage = new LinkedHashMap<>();
        for (CycleTimeFactEntity fact : filtered) {
            byStage.computeIfAbsent(fact.getStage(), ignored -> new ArrayList<>()).add(fact);
        }

        List<Integer> allMinutes = filtered.stream().map(CycleTimeFactEntity::getCycleTimeMinutes).toList();
        String bottleneckStage = byStage.entrySet().stream()
                .max(java.util.Comparator.comparingDouble(entry -> entry.getValue().stream().mapToInt(CycleTimeFactEntity::getCycleTimeMinutes).average().orElse(0)))
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        List<HeadlineMetricDto> headline = allMinutes.isEmpty()
                ? List.of()
                : List.of(
                        new HeadlineMetricDto("avg", "Average cycle time", ReportAggregationSupport.formatMinutes((int) ReportAggregationSupport.average(allMinutes)), ReportAggregationSupport.average(allMinutes), -4.4, true),
                        new HeadlineMetricDto("bottleneck", "Slowest stage", ReportAggregationSupport.titleize(bottleneckStage), 1, 3.2, false),
                        new HeadlineMetricDto("samples", "Observed samples", String.valueOf(allMinutes.size()), allMinutes.size(), 1.9, true)
                );

        List<SeriesPointDto> series = filtered.stream()
                .map(fact -> new SeriesPointDto(
                        seriesGroupKey(fact, context.request().grouping()),
                        seriesGroupLabel(fact, context.request().grouping()),
                        ReportAggregationSupport.titleize(fact.getStage()),
                        fact.getCycleTimeMinutes(),
                        null
                ))
                .toList();

        List<Map<String, Object>> rows = filtered.stream()
                .map(fact -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("stage", ReportAggregationSupport.titleize(fact.getStage()));
                    row.put("team", seriesGroupLabel(fact, context.request().grouping()));
                    row.put("avgMin", fact.getCycleTimeMinutes());
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

    private String seriesGroupKey(CycleTimeFactEntity fact, String grouping) {
        return switch (grouping) {
            case "team" -> fact.getTeamId();
            case "project" -> fact.getProjectId();
            default -> fact.getTeamId();
        };
    }

    private String seriesGroupLabel(CycleTimeFactEntity fact, String grouping) {
        return switch (grouping) {
            case "project" -> projectSpaceSeedCatalog.project(fact.getProjectId()).name();
            default -> ReportAggregationSupport.titleize(fact.getTeamId());
        };
    }
}
