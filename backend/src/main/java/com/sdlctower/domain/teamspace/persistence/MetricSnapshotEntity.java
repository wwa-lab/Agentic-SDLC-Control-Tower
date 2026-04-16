package com.sdlctower.domain.teamspace.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "metric_snapshots")
public class MetricSnapshotEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "metric_group", nullable = false)
    private String metricGroup;

    @Column(name = "metric_key", nullable = false)
    private String metricKey;

    @Column(name = "metric_label", nullable = false)
    private String metricLabel;

    @Column(name = "metric_unit", nullable = false)
    private String metricUnit;

    @Column(name = "current_value", nullable = false)
    private double currentValue;

    @Column(name = "previous_value", nullable = false)
    private double previousValue;

    @Column(name = "trend_direction", nullable = false)
    private String trendDirection;

    @Column(name = "history_url", nullable = false)
    private String historyUrl;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String tooltip;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    protected MetricSnapshotEntity() {}

    public static MetricSnapshotEntity create(
            String id,
            String workspaceId,
            String metricGroup,
            String metricKey,
            String metricLabel,
            String metricUnit,
            double currentValue,
            double previousValue,
            String trendDirection,
            String historyUrl,
            String tooltip,
            Instant snapshotAt
    ) {
        MetricSnapshotEntity entity = new MetricSnapshotEntity();
        entity.id = id;
        entity.workspaceId = workspaceId;
        entity.metricGroup = metricGroup;
        entity.metricKey = metricKey;
        entity.metricLabel = metricLabel;
        entity.metricUnit = metricUnit;
        entity.currentValue = currentValue;
        entity.previousValue = previousValue;
        entity.trendDirection = trendDirection;
        entity.historyUrl = historyUrl;
        entity.tooltip = tooltip;
        entity.snapshotAt = snapshotAt;
        return entity;
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getMetricGroup() { return metricGroup; }
    public String getMetricKey() { return metricKey; }
    public String getMetricLabel() { return metricLabel; }
    public String getMetricUnit() { return metricUnit; }
    public double getCurrentValue() { return currentValue; }
    public double getPreviousValue() { return previousValue; }
    public String getTrendDirection() { return trendDirection; }
    public String getHistoryUrl() { return historyUrl; }
    public String getTooltip() { return tooltip; }
    public Instant getSnapshotAt() { return snapshotAt; }
}
