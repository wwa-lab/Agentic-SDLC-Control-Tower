package com.sdlctower.domain.teamspace;

import com.sdlctower.domain.teamspace.persistence.MetricSnapshotEntity;
import com.sdlctower.domain.teamspace.persistence.MetricSnapshotRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricsSnapshotJob {

    private final MetricSnapshotRepository metricSnapshotRepository;

    public MetricsSnapshotJob(MetricSnapshotRepository metricSnapshotRepository) {
        this.metricSnapshotRepository = metricSnapshotRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void refresh() {
        if (metricSnapshotRepository.count() > 0) {
            return;
        }

        metricSnapshotRepository.save(MetricSnapshotEntity.create(
                "MET-AUTO-0001",
                "ws-default-001",
                "deliveryEfficiency",
                "delivery.cycleTime",
                "Cycle Time",
                "DAYS",
                4.0,
                4.6,
                "DOWN",
                "/reports/metric/delivery.cycleTime?workspaceId=ws-default-001",
                "Auto-refresh restored baseline metric coverage for Team Space",
                Instant.now()
        ));
    }
}
