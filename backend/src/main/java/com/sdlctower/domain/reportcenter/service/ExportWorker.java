package com.sdlctower.domain.reportcenter.service;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.entity.ReportExport;
import com.sdlctower.domain.reportcenter.render.CsvWriter;
import com.sdlctower.domain.reportcenter.render.PdfRenderer;
import com.sdlctower.domain.reportcenter.repository.ReportExportRepository;
import com.sdlctower.domain.reportcenter.storage.ArtifactStore;
import com.sdlctower.domain.reportcenter.storage.ReportSigningService;
import com.sdlctower.shared.audit.AuditEventService;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExportWorker {

    private final ReportExportRepository reportExportRepository;
    private final CsvWriter csvWriter;
    private final PdfRenderer pdfRenderer;
    private final ArtifactStore artifactStore;
    private final ReportSigningService signingService;
    private final AuditEventService auditEventService;

    public ExportWorker(
            ReportExportRepository reportExportRepository,
            CsvWriter csvWriter,
            PdfRenderer pdfRenderer,
            ArtifactStore artifactStore,
            ReportSigningService signingService,
            AuditEventService auditEventService
    ) {
        this.reportExportRepository = reportExportRepository;
        this.csvWriter = csvWriter;
        this.pdfRenderer = pdfRenderer;
        this.artifactStore = artifactStore;
        this.signingService = signingService;
        this.auditEventService = auditEventService;
    }

    @Async("reportCenterExecutor")
    @Transactional
    public void generate(String exportId, ReportRunResultDto result) {
        ReportExport export = reportExportRepository.findById(exportId)
                .orElseThrow(() -> new ResourceNotFoundException("EXPORT_NOT_FOUND: " + exportId));
        export.markGenerating();

        try {
            if (result.drilldown().data() != null && "csv".equals(export.getFormat()) && result.drilldown().data().totalRows() > 100000) {
                throw new IllegalArgumentException("EXPORT_TOO_LARGE: Result set exceeds 100000 rows");
            }

            byte[] bytes = render(export.getFormat(), export.getReportKey(), result);
            String filename = export.getId() + "." + export.getFormat();
            String storagePath = artifactStore.save(filename, bytes);
            long expiresAt = export.getExpiresAt().getEpochSecond();
            String downloadUrl = "/api/v1/reports/exports/" + export.getId() + "/file?sig="
                    + signingService.sign(export.getId(), export.getUserId(), expiresAt)
                    + "&exp=" + expiresAt;
            export.markReady(downloadUrl, storagePath, bytes.length, Instant.now());
            auditEventService.record(new AuditEventService.AuditEvent(
                    "report.export",
                    export.getUserId(),
                    export.getReportKey(),
                    Instant.now(),
                    Map.of(
                            "scope", result.scope(),
                            "scopeIds", result.scopeIds(),
                            "timeRange", result.timeRange(),
                            "grouping", result.grouping(),
                            "format", export.getFormat(),
                            "exportId", export.getId(),
                            "rowCount", result.drilldown().data() == null ? 0 : result.drilldown().data().totalRows(),
                            "bytes", bytes.length
                    )
            ));
        } catch (Exception ex) {
            export.markFailed(ex.getMessage());
        }
    }

    private byte[] render(String format, String reportKey, ReportRunResultDto result) {
        DrilldownDto drilldown = result.drilldown().data() == null
                ? new DrilldownDto(java.util.List.of(), java.util.List.of(), 0)
                : result.drilldown().data();
        return "pdf".equals(format)
                ? pdfRenderer.render(reportKey, result)
                : csvWriter.write(drilldown);
    }
}
