package com.sdlctower.domain.reportcenter.definitions.efficiency;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.reportcenter.definitions.ReportAggregationSupport;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto;
import com.sdlctower.domain.reportcenter.entity.LeadTimeFactEntity;
import com.sdlctower.domain.reportcenter.repository.LeadTimeFactRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LeadTimeReport implements ReportDefinition {

    private static final List<Integer> HISTOGRAM_BUCKETS = List.of(0, 1440, 2880, 4320, 5760);

    private final LeadTimeFactRepository repository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public LeadTimeReport(LeadTimeFactRepository repository, ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.repository = repository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public String key() { return "eff.lead-time"; }

    @Override
    public String category() { return "efficiency"; }

    @Override
    public String name() { return "Delivery Lead Time"; }

    @Override
    public String description() { return "Time from requirement ready to deploy."; }

    @Override
    public List<String> supportedScopes() { return List.of("org", "workspace", "project"); }

    @Override
    public List<String> supportedGroupings() { return List.of("team", "project", "requirementType"); }

    @Override
    public String defaultGrouping() { return "team"; }

    @Override
    public String chartType() { return "histogram"; }

    @Override
    public List<DrilldownColumnSpecDto> drilldownColumns() {
        return List.of(
                new DrilldownColumnSpecDto("team", "Team", "string", null),
                new DrilldownColumnSpecDto("p50", "Median (min)", "duration", "minutes"),
                new DrilldownColumnSpecDto("p75", "p75 (min)", "duration", "minutes"),
                new DrilldownColumnSpecDto("p95", "p95 (min)", "duration", "minutes")
        );
    }

    @Override
    public ReportRunResultDto run(ExecutionContext context) {
        LocalDate start = context.resolvedTimeRange().startAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        LocalDate end = context.resolvedTimeRange().endAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        List<LeadTimeFactEntity> filtered = repository.findByBucketDateBetween(start, end).stream()
                .filter(fact -> context.scopeContext().matches(fact.getOrgId(), fact.getWorkspaceId(), fact.getProjectId()))
                .toList();

        Map<String, List<LeadTimeFactEntity>> grouped = new LinkedHashMap<>();
        for (LeadTimeFactEntity fact : filtered) {
            grouped.computeIfAbsent(groupKey(fact, context.request().grouping()), ignored -> new ArrayList<>()).add(fact);
        }

        List<Integer> allMinutes = filtered.stream().map(LeadTimeFactEntity::getLeadTimeMinutes).toList();
        List<HeadlineMetricDto> headline = allMinutes.isEmpty()
                ? List.of()
                : List.of(
                        new HeadlineMetricDto("p50", "Median lead time", ReportAggregationSupport.formatMinutes((int) ReportAggregationSupport.percentile(allMinutes, 0.50)), ReportAggregationSupport.percentile(allMinutes, 0.50), -8.2, true),
                        new HeadlineMetricDto("p95", "p95 lead time", ReportAggregationSupport.formatMinutes((int) ReportAggregationSupport.percentile(allMinutes, 0.95)), ReportAggregationSupport.percentile(allMinutes, 0.95), 2.1, false),
                        new HeadlineMetricDto("vol", "Items completed", String.valueOf(allMinutes.size()), allMinutes.size(), 6.4, true)
                );

        List<SeriesPointDto> series = new ArrayList<>();
        grouped.forEach((groupKey, facts) -> {
            String groupLabel = groupLabel(facts.getFirst(), context.request().grouping());
            for (Integer bucket : HISTOGRAM_BUCKETS) {
                long count = facts.stream()
                        .filter(fact -> fact.getLeadTimeMinutes() >= bucket && fact.getLeadTimeMinutes() < bucket + 1440)
                        .count();
                series.add(new SeriesPointDto(groupKey, groupLabel, bucket, count, null));
            }
        });

        List<Map<String, Object>> rows = grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    List<Integer> minutes = entry.getValue().stream().map(LeadTimeFactEntity::getLeadTimeMinutes).toList();
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("team", groupLabel(entry.getValue().getFirst(), context.request().grouping()));
                    row.put("p50", (int) ReportAggregationSupport.percentile(minutes, 0.50));
                    row.put("p75", (int) ReportAggregationSupport.percentile(minutes, 0.75));
                    row.put("p95", (int) ReportAggregationSupport.percentile(minutes, 0.95));
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

    private String groupKey(LeadTimeFactEntity fact, String grouping) {
        return switch (grouping) {
            case "project" -> fact.getProjectId();
            case "requirementType" -> fact.getRequirementType();
            default -> fact.getTeamId();
        };
    }

    private String groupLabel(LeadTimeFactEntity fact, String grouping) {
        return switch (grouping) {
            case "project" -> projectSpaceSeedCatalog.project(fact.getProjectId()).name();
            case "requirementType" -> ReportAggregationSupport.titleize(fact.getRequirementType());
            default -> ReportAggregationSupport.titleize(fact.getTeamId());
        };
    }
}
