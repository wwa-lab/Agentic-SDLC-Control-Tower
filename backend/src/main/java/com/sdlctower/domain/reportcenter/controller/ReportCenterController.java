package com.sdlctower.domain.reportcenter.controller;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.CatalogDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ExportJobDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportExportHistoryEntryDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunHistoryEntryDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunRequestDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import com.sdlctower.domain.reportcenter.service.ReportCatalogService;
import com.sdlctower.domain.reportcenter.service.ReportExportService;
import com.sdlctower.domain.reportcenter.service.ReportHistoryService;
import com.sdlctower.domain.reportcenter.service.ReportRunService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.REPORTS_BASE)
public class ReportCenterController {

    private final ReportCatalogService catalogService;
    private final ReportRunService reportRunService;
    private final ReportHistoryService reportHistoryService;
    private final ReportExportService reportExportService;

    public ReportCenterController(
            ReportCatalogService catalogService,
            ReportRunService reportRunService,
            ReportHistoryService reportHistoryService,
            ReportExportService reportExportService
    ) {
        this.catalogService = catalogService;
        this.reportRunService = reportRunService;
        this.reportHistoryService = reportHistoryService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/catalog")
    public ApiResponse<CatalogDto> getCatalog() {
        return ApiResponse.ok(catalogService.getCatalogForCaller());
    }

    @PostMapping("/{reportKey}/run")
    public ResponseEntity<ApiResponse<ReportRunResultDto>> run(
            @PathVariable String reportKey,
            @Valid @RequestBody ReportRunRequestDto request
    ) {
        ReportRunService.RunExecution execution = reportRunService.run(reportKey, request);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (Boolean.TRUE.equals(execution.result().slow())) {
            builder.header("X-Report-Slow", "true");
        }
        return builder.body(ApiResponse.ok(execution.result()));
    }

    @PostMapping("/{reportKey}/export")
    public ResponseEntity<ApiResponse<ExportJobDto>> export(
            @PathVariable String reportKey,
            @Valid @RequestBody ReportRunRequestDto request,
            @RequestParam String format
    ) {
        return ResponseEntity.accepted().body(ApiResponse.ok(reportExportService.enqueue(reportKey, request, format)));
    }

    @GetMapping("/exports/{exportId}")
    public ApiResponse<ExportJobDto> getExport(@PathVariable String exportId) {
        return ApiResponse.ok(reportExportService.getForCaller(exportId));
    }

    @GetMapping("/exports/{exportId}/file")
    public ResponseEntity<Resource> downloadExport(
            @PathVariable String exportId,
            @RequestParam("sig") String signature,
            @RequestParam("exp") long expiresAtEpochSeconds
    ) throws IOException {
        ReportExportService.DownloadPayload payload = reportExportService.downloadForCaller(exportId, signature, expiresAtEpochSeconds);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.filename() + "\"")
                .contentType(payload.mediaType())
                .body(payload.resource());
    }

    @GetMapping("/history")
    public ApiResponse<List<ReportRunHistoryEntryDto>> history(@RequestParam(required = false) String reportKey) {
        return ApiResponse.ok(reportHistoryService.forCaller(reportKey));
    }

    @GetMapping("/exports")
    public ApiResponse<List<ReportExportHistoryEntryDto>> exportHistory() {
        return ApiResponse.ok(reportHistoryService.exportsForCaller());
    }
}
