package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceConstants;
import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.TeamMetricItemDto;
import com.sdlctower.domain.teamspace.dto.TeamMetricsDto;
import com.sdlctower.domain.teamspace.persistence.MetricSnapshotEntity;
import com.sdlctower.domain.teamspace.persistence.MetricSnapshotRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MetricsProjection implements TeamSpaceProjection<TeamMetricsDto> {

    private final TeamSpaceSeedCatalog seedCatalog;
    private final MetricSnapshotRepository metricSnapshotRepository;

    public MetricsProjection(TeamSpaceSeedCatalog seedCatalog, MetricSnapshotRepository metricSnapshotRepository) {
        this.seedCatalog = seedCatalog;
        this.metricSnapshotRepository = metricSnapshotRepository;
    }

    @Override
    public TeamMetricsDto load(String workspaceId) {
        seedCatalog.workspace(workspaceId);
        if ("ws-degraded-001".equals(workspaceId)) {
            throw new IllegalStateException("Metrics projection unavailable for workspace " + workspaceId);
        }

        List<MetricSnapshotEntity> snapshots = metricSnapshotRepository.findByWorkspaceIdOrderBySnapshotAtDesc(workspaceId);
        Map<String, List<TeamMetricItemDto>> grouped = snapshots.stream()
                .sorted(Comparator.comparing(MetricSnapshotEntity::getMetricKey))
                .collect(Collectors.groupingBy(
                        MetricSnapshotEntity::getMetricGroup,
                        java.util.LinkedHashMap::new,
                        Collectors.mapping(snapshot -> new TeamMetricItemDto(
                                snapshot.getMetricKey(),
                                snapshot.getMetricLabel(),
                                snapshot.getCurrentValue(),
                                snapshot.getPreviousValue(),
                                snapshot.getMetricUnit(),
                                snapshot.getTrendDirection(),
                                snapshot.getHistoryUrl(),
                                snapshot.getTooltip()
                        ), Collectors.toList())
                ));

        Instant lastRefreshed = snapshots.stream()
                .map(MetricSnapshotEntity::getSnapshotAt)
                .max(Comparator.naturalOrder())
                .orElse(TeamSpaceConstants.REFERENCE_NOW);

        return new TeamMetricsDto(
                grouped.getOrDefault("deliveryEfficiency", List.of()),
                grouped.getOrDefault("quality", List.of()),
                grouped.getOrDefault("stability", List.of()),
                grouped.getOrDefault("governanceMaturity", List.of()),
                grouped.getOrDefault("aiParticipation", List.of()),
                lastRefreshed
        );
    }
}
