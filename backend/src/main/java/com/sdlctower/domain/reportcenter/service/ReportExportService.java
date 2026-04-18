package com.sdlctower.domain.reportcenter.service;

import com.sdlctower.domain.projectmanagement.policy.ProjectManagementActorResolver;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ExportJobDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunRequestDto;
import com.sdlctower.domain.reportcenter.entity.ReportExport;
import com.sdlctower.domain.reportcenter.repository.ReportExportRepository;
import com.sdlctower.domain.reportcenter.storage.ArtifactStore;
import com.sdlctower.domain.reportcenter.storage.ReportSigningService;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportExportService {

    private final ReportRunService reportRunService;
    private final ReportExportRepository reportExportRepository;
    private final ProjectManagementActorResolver actorResolver;
    private final ArtifactStore artifactStore;
    private final ReportSigningService signingService;
    private final ExportWorker exportWorker;

    public ReportExportService(
            ReportRunService reportRunService,
            ReportExportRepository reportExportRepository,
            ProjectManagementActorResolver actorResolver,
            ArtifactStore artifactStore,
            ReportSigningService signingService,
            ExportWorker exportWorker
    ) {
        this.reportRunService = reportRunService;
        this.reportExportRepository = reportExportRepository;
        this.actorResolver = actorResolver;
        this.artifactStore = artifactStore;
        this.signingService = signingService;
        this.exportWorker = exportWorker;
    }

    public ExportJobDto enqueue(String reportKey, ReportRunRequestDto request, String format) {
        if (!"csv".equalsIgnoreCase(format) && !"pdf".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        ReportRunService.RunExecution runExecution = reportRunService.run(reportKey, request);
        String actorId = actorResolver.currentActor().memberId();
        ReportExport export = ReportExport.create(
                UUID.randomUUID().toString(),
                runExecution.entity().getId(),
                actorId,
                reportKey,
                format.toLowerCase(),
                Instant.now(),
                Instant.now().plus(Duration.ofDays(7))
        );
        reportExportRepository.saveAndFlush(export);
        exportWorker.generate(export.getId(), runExecution.result());
        return toDto(export);
    }

    public ExportJobDto getForCaller(String exportId) {
        String actorId = actorResolver.currentActor().memberId();
        ReportExport export = reportExportRepository.findByIdAndUserId(exportId, actorId)
                .orElseThrow(() -> new ResourceNotFoundException("EXPORT_NOT_FOUND: " + exportId));
        return toDto(export);
    }

    public DownloadPayload downloadForCaller(String exportId, String signature, long expiresAtEpochSeconds) throws IOException {
        String actorId = actorResolver.currentActor().memberId();
        ReportExport export = reportExportRepository.findByIdAndUserId(exportId, actorId)
                .orElseThrow(() -> new ResourceNotFoundException("EXPORT_NOT_FOUND: " + exportId));
        if (!signingService.verify(exportId, actorId, signature, expiresAtEpochSeconds)) {
            throw new IllegalArgumentException("Invalid or expired export signature");
        }
        if (export.getStoragePath() == null) {
            throw new ResourceNotFoundException("Export file is not ready");
        }
        Resource resource = artifactStore.read(export.getStoragePath());
        MediaType mediaType = "pdf".equals(export.getFormat()) ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");
        String filename = export.getReportKey().replace('.', '-') + "." + export.getFormat();
        return new DownloadPayload(resource, mediaType, filename);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireArtifacts() {
        for (ReportExport export : reportExportRepository.findByStatusAndExpiresAtBefore("ready", Instant.now())) {
            try {
                artifactStore.delete(export.getStoragePath());
            } catch (IOException ignored) {
                // Retention best-effort cleanup.
            }
            export.markExpired();
        }
    }

    private ExportJobDto toDto(ReportExport export) {
        return new ExportJobDto(
                export.getId(),
                export.getReportKey(),
                export.getFormat(),
                export.getStatus(),
                export.getDownloadUrl(),
                export.getErrorMessage(),
                export.getBytes(),
                export.getCreatedAt(),
                export.getReadyAt(),
                export.getExpiresAt()
        );
    }

    public record DownloadPayload(Resource resource, MediaType mediaType, String filename) {}
}
