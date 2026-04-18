package com.sdlctower.domain.reportcenter.definitions.efficiency;

import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.reportcenter.definitions.ReportAggregationSupport;
import com.sdlctower.domain.reportcenter.definitions.ReportDefinition;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownColumnSpecDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.HeadlineMetricDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.SeriesPointDto;
import com.sdlctower.domain.reportcenter.entity.WipFactEntity;
import com.sdlctower.domain.reportcenter.repository.WipFactRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WipReport implements ReportDefinition {

    private final WipFactRepository repository;
    private final ProjectSpaceSeedCatalog projectSpaceSeedCatalog;

    public WipReport(WipFactRepository repository, ProjectSpaceSeedCatalog projectSpaceSeedCatalog) {
        this.repository = repository;
        this.projectSpaceSeedCatalog = projectSpaceSeedCatalog;
    }

    @Override
    public String key() { return "eff.wip"; }

    @Override
    public String category() { return "efficiency"; }

    @Override
    public String name() { return "Work In Progress"; }

    @Override
    public String description() { return "Active items per stage with aging buckets."; }

    @Override
    public List<String> supportedScopes() { return List.of("org", "workspace", "project"); }

    @Override
    public List<String> supportedGroupings() { return List.of("stage-team", "owner-team"); }

    @Override
    public String defaultGrouping() { return "stage-team"; }

    @Override
    public String chartType() { return "heatmap"; }

    @Override
    public List<DrilldownColumnSpecDto> drilldownColumns() {
        return List.of(
                new DrilldownColumnSpecDto("stage", "Stage", "string", null),
                new DrilldownColumnSpecDto("team", "Team", "string", null),
                new DrilldownColumnSpecDto("ageBucket", "Age", "string", null),
                new DrilldownColumnSpecDto("count", "Count", "number", null)
        );
    }

    @Override
    public ReportRunResultDto run(ExecutionContext context) {
        List<WipFactEntity> filtered = repository.findBySnapshotAtBetween(context.resolvedTimeRange().startAt(), context.resolvedTimeRange().endAt()).stream()
                .filter(fact -> context.scopeContext().matches(fact.getOrgId(), fact.getWorkspaceId(), fact.getProjectId()))
                .toList();

        Instant latestSnapshot = filtered.stream().map(WipFactEntity::getSnapshotAt).max(Instant::compareTo).orElse(context.snapshotAt());
        List<WipFactEntity> latest = filtered.stream()
                .filter(fact -> fact.getSnapshotAt().equals(latestSnapshot))
                .toList();

        int total = latest.stream().mapToInt(WipFactEntity::getItemCount).sum();
        int aged = latest.stream()
                .filter(fact -> "7-14d".equals(fact.getAgeBucket()) || "14d+".equals(fact.getAgeBucket()))
                .mapToInt(WipFactEntity::getItemCount)
                .sum();
        String hottestStage = latest.stream()
                .max(java.util.Comparator.comparingInt(WipFactEntity::getItemCount))
                .map(WipFactEntity::getStage)
                .orElse("Unknown");

        List<HeadlineMetricDto> headline = latest.isEmpty()
                ? List.of()
                : List.of(
                        new HeadlineMetricDto("open", "Open items", String.valueOf(total), total, 8.1, false),
                        new HeadlineMetricDto("aged", "Aged > 7d", String.valueOf(aged), aged, 4.8, false),
                        new HeadlineMetricDto("hot", "Hottest stage", ReportAggregationSupport.titleize(hottestStage), 1, 0.0, null)
                );

        List<SeriesPointDto> series = latest.stream()
                .map(fact -> new SeriesPointDto(
                        groupKey(fact, context.request().grouping()),
                        groupLabel(fact, context.request().grouping()),
                        ReportAggregationSupport.titleize(fact.getStage()) + "|" + fact.getAgeBucket(),
                        fact.getItemCount(),
                        null
                ))
                .toList();

        List<Map<String, Object>> rows = latest.stream()
                .map(fact -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("stage", ReportAggregationSupport.titleize(fact.getStage()));
                    row.put("team", groupLabel(fact, context.request().grouping()));
                    row.put("ageBucket", fact.getAgeBucket());
                    row.put("count", fact.getItemCount());
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

    private String groupKey(WipFactEntity fact, String grouping) {
        return "owner-team".equals(grouping) ? fact.getOwnerId() : fact.getTeamId();
    }

    private String groupLabel(WipFactEntity fact, String grouping) {
        return "owner-team".equals(grouping)
                ? projectSpaceSeedCatalog.memberDisplayName(fact.getOwnerId())
                : ReportAggregationSupport.titleize(fact.getTeamId());
    }
}
