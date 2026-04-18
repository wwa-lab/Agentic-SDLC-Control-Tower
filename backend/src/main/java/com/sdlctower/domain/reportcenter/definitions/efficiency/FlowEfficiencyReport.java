package com.sdlctower.domain.reportcenter.definitions.efficiency;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.reportcenter.definitions.ReportAggregationSupport;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto;
import com.sdlctower.domain.reportcenter.entity.FlowEfficiencyFactEntity;
import com.sdlctower.domain.reportcenter.repository.FlowEfficiencyFactRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FlowEfficiencyReport implements ReportDefinition {

    private final FlowEfficiencyFactRepository repository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public FlowEfficiencyReport(FlowEfficiencyFactRepository repository, ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.repository = repository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public String key() { return "eff.flow-efficiency"; }

    @Override
    public String category() { return "efficiency"; }

    @Override
    public String name() { return "Flow Efficiency"; }

    @Override
    public String description() { return "Active work time divided by total cycle time per stage."; }

    @Override
    public List<String> supportedScopes() { return List.of("org", "workspace", "project"); }

    @Override
    public List<String> supportedGroupings() { return List.of("stage", "team"); }

    @Override
    public String defaultGrouping() { return "stage"; }

    @Override
    public String chartType() { return "horizontal-bar"; }

    @Override
    public List<DrilldownColumnSpecDto> drilldownColumns() {
        return List.of(
                new DrilldownColumnSpecDto("stage", "Stage", "string", null),
                new DrilldownColumnSpecDto("flowEff", "Flow Eff.", "number", "percent-1"),
                new DrilldownColumnSpecDto("activeMin", "Active (min)", "duration", "minutes"),
                new DrilldownColumnSpecDto("totalMin", "Total (min)", "duration", "minutes")
        );
    }

    @Override
    public ReportRunResultDto run(ExecutionContext context) {
        LocalDate start = context.resolvedTimeRange().startAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        LocalDate end = context.resolvedTimeRange().endAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        List<FlowEfficiencyFactEntity> filtered = repository.findByBucketDateBetween(start, end).stream()
                .filter(fact -> context.scopeContext().matches(fact.getOrgId(), fact.getWorkspaceId(), fact.getProjectId()))
                .toList();

        Map<String, List<FlowEfficiencyFactEntity>> grouped = new LinkedHashMap<>();
        for (FlowEfficiencyFactEntity fact : filtered) {
            grouped.computeIfAbsent(groupKey(fact, context.request().grouping()), ignored -> new java.util.ArrayList<>()).add(fact);
        }

        List<Map.Entry<String, Double>> efficiencies = grouped.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), efficiency(entry.getValue())))
                .toList();

        double average = efficiencies.stream().mapToDouble(Map.Entry::getValue).average().orElse(0);
        Map.Entry<String, Double> best = efficiencies.stream().max(Map.Entry.comparingByValue()).orElse(Map.entry("n/a", 0.0));
        Map.Entry<String, Double> lowest = efficiencies.stream().min(Map.Entry.comparingByValue()).orElse(Map.entry("n/a", 0.0));

        List<HeadlineMetricDto> headline = efficiencies.isEmpty()
                ? List.of()
                : List.of(
                        new HeadlineMetricDto("avg", "Average flow efficiency", String.format(java.util.Locale.US, "%.1f%%", average), average, 2.4, true),
                        new HeadlineMetricDto("best", "Best stage", groupDisplay(best.getKey(), context.request().grouping()), 1, null, null),
                        new HeadlineMetricDto("drag", "Largest drag", groupDisplay(lowest.getKey(), context.request().grouping()), 1, null, null)
                );

        List<SeriesPointDto> series = grouped.entrySet().stream()
                .map(entry -> new SeriesPointDto(
                        entry.getKey(),
                        groupDisplay(entry.getKey(), context.request().grouping()),
                        groupDisplay(entry.getKey(), context.request().grouping()),
                        efficiency(entry.getValue()),
                        null
                ))
                .toList();

        List<Map<String, Object>> rows = grouped.entrySet().stream()
                .map(entry -> {
                    int active = entry.getValue().stream().mapToInt(FlowEfficiencyFactEntity::getActiveMinutes).sum();
                    int totalMinutes = entry.getValue().stream().mapToInt(FlowEfficiencyFactEntity::getTotalMinutes).sum();
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("stage", groupDisplay(entry.getKey(), context.request().grouping()));
                    row.put("flowEff", efficiency(entry.getValue()));
                    row.put("activeMin", active);
                    row.put("totalMin", totalMinutes);
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

    private String groupKey(FlowEfficiencyFactEntity fact, String grouping) {
        return "team".equals(grouping) ? fact.getTeamId() : fact.getStage();
    }

    private String groupDisplay(String key, String grouping) {
        return "team".equals(grouping)
                ? ReportAggregationSupport.titleize(key)
                : ReportAggregationSupport.titleize(key);
    }

    private double efficiency(List<FlowEfficiencyFactEntity> facts) {
        int active = facts.stream().mapToInt(FlowEfficiencyFactEntity::getActiveMinutes).sum();
        int total = facts.stream().mapToInt(FlowEfficiencyFactEntity::getTotalMinutes).sum();
        return total == 0 ? 0 : Math.round((active * 1000.0 / total)) / 10.0;
    }
}
