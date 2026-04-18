package com.sdlctower.domain.reportcenter.service;

import com.sdlctower.domain.reportcenter.definitions.ReportDefinitionRegistry;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportExportHistoryEntryDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunHistoryEntryDto;
import com.sdlctower.domain.reportcenter.entity.ReportExport;
import com.sdlctower.domain.reportcenter.entity.ReportRun;
import com.sdlctower.domain.reportcenter.repository.ReportExportRepository;
import com.sdlctower.domain.reportcenter.repository.ReportRunRepository;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportHistoryService {

    private final ReportRunRepository reportRunRepository;
    private final ReportExportRepository reportExportRepository;
    private final ReportDefinitionRegistry registry;
    private final ProjectManagementActorResolver actorResolver;

    public ReportHistoryService(
            ReportRunRepository reportRunRepository,
            ReportExportRepository reportExportRepository,
            ReportDefinitionRegistry registry,
            ProjectManagementActorResolver actorResolver
    ) {
        this.reportRunRepository = reportRunRepository;
        this.reportExportRepository = reportExportRepository;
        this.registry = registry;
        this.actorResolver = actorResolver;
    }

    public List<ReportRunHistoryEntryDto> forCaller(String reportKey) {
        String actorId = actorResolver.currentActor().memberId();
        List<ReportRun> rows = reportKey == null || reportKey.isBlank()
                ? reportRunRepository.findTop50ByUserIdOrderByRunAtDesc(actorId)
                : reportRunRepository.findTop50ByUserIdAndReportKeyOrderByRunAtDesc(actorId, reportKey);
        return rows.stream().map(this::toRunHistory).toList();
    }

    public List<ReportExportHistoryEntryDto> exportsForCaller() {
        String actorId = actorResolver.currentActor().memberId();
        Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(7));
        return reportExportRepository.findTop50ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(actorId, cutoff).stream()
                .map(this::toExportHistory)
                .toList();
    }

    private ReportRunHistoryEntryDto toRunHistory(ReportRun entity) {
        String reportName = registry.require(entity.getReportKey()).name();
        String scopeSummary = entity.getScope().substring(0, 1).toUpperCase() + entity.getScope().substring(1) + ": " + entity.getScopeIdsJson().replace("[", "").replace("]", "").split(",").length;
        String timeRangeLabel = switch (entity.getTimeRangePreset()) {
            case "last7d" -> "Last 7 days";
            case "last30d" -> "Last 30 days";
            case "last90d" -> "Last 90 days";
            case "qtd" -> "Quarter to date";
            case "ytd" -> "Year to date";
            default -> "Custom range";
        };
        return new ReportRunHistoryEntryDto(
                entity.getId(),
                entity.getReportKey(),
                reportName,
                entity.getScope(),
                scopeSummary,
                timeRangeLabel,
                entity.getGrouping(),
                entity.getRunAt(),
                entity.getDurationMs()
        );
    }

    private ReportExportHistoryEntryDto toExportHistory(ReportExport entity) {
        String reportName = registry.require(entity.getReportKey()).name();
        return new ReportExportHistoryEntryDto(
                entity.getId(),
                entity.getReportKey(),
                reportName,
                entity.getFormat(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getDownloadUrl(),
                entity.getBytes()
        );
    }
}
