package com.sdlctower.domain.reportcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "report_run")
public class ReportRun {

    @Id
    private String id;

    @Column(name = "report_key", nullable = false)
    private String reportKey;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String scope;

    @Column(name = "scope_ids_json", nullable = false, columnDefinition = "CLOB")
    private String scopeIdsJson;

    @Column(name = "time_range_preset", nullable = false)
    private String timeRangePreset;

    @Column(name = "time_range_start")
    private Instant timeRangeStart;

    @Column(name = "time_range_end")
    private Instant timeRangeEnd;

    @Column(nullable = false)
    private String grouping;

    @Column(name = "extra_filters_json", columnDefinition = "CLOB")
    private String extraFiltersJson;

    @Column(name = "run_at", nullable = false)
    private Instant runAt;

    @Column(name = "duration_ms", nullable = false)
    private int durationMs;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    protected ReportRun() {}

    public static ReportRun create(
            String id,
            String reportKey,
            String userId,
            String scope,
            String scopeIdsJson,
            String timeRangePreset,
            Instant timeRangeStart,
            Instant timeRangeEnd,
            String grouping,
            String extraFiltersJson,
            Instant runAt,
            int durationMs,
            int rowCount
    ) {
        ReportRun entity = new ReportRun();
        entity.id = id;
        entity.reportKey = reportKey;
        entity.userId = userId;
        entity.scope = scope;
        entity.scopeIdsJson = scopeIdsJson;
        entity.timeRangePreset = timeRangePreset;
        entity.timeRangeStart = timeRangeStart;
        entity.timeRangeEnd = timeRangeEnd;
        entity.grouping = grouping;
        entity.extraFiltersJson = extraFiltersJson;
        entity.runAt = runAt;
        entity.durationMs = durationMs;
        entity.rowCount = rowCount;
        return entity;
    }

    public String getId() { return id; }
    public String getReportKey() { return reportKey; }
    public String getUserId() { return userId; }
    public String getScope() { return scope; }
    public String getScopeIdsJson() { return scopeIdsJson; }
    public String getTimeRangePreset() { return timeRangePreset; }
    public Instant getTimeRangeStart() { return timeRangeStart; }
    public Instant getTimeRangeEnd() { return timeRangeEnd; }
    public String getGrouping() { return grouping; }
    public String getExtraFiltersJson() { return extraFiltersJson; }
    public Instant getRunAt() { return runAt; }
    public int getDurationMs() { return durationMs; }
    public int getRowCount() { return rowCount; }
}
