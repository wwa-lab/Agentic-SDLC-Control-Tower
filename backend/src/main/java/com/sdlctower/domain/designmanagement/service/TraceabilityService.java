package com.sdlctower.domain.designmanagement.service;

import com.sdlctower.domain.designmanagement.DesignManagementConstants;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CoverageBucketDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityAggregateDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityCellDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityGapDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilityMatrixRowDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.TraceabilitySummaryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.CoverageStatus;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkRepository;
import com.sdlctower.domain.designmanagement.policy.DesignAccessGuard;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TraceabilityService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignSpecLinkRepository linkRepository;
    private final SpecRevisionLookup specRevisionLookup;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final CoverageService coverageService;
    private final DesignAccessGuard accessGuard;
    private final Executor executor;

    @Autowired
    public TraceabilityService(
            DesignArtifactRepository artifactRepository,
            DesignSpecLinkRepository linkRepository,
            SpecRevisionLookup specRevisionLookup,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CoverageService coverageService,
            DesignAccessGuard accessGuard
    ) {
        this(
                artifactRepository,
                linkRepository,
                specRevisionLookup,
                projectSeedCatalog,
                coverageService,
                accessGuard,
                ForkJoinPool.commonPool()
        );
    }

    TraceabilityService(
            DesignArtifactRepository artifactRepository,
            DesignSpecLinkRepository linkRepository,
            SpecRevisionLookup specRevisionLookup,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CoverageService coverageService,
            DesignAccessGuard accessGuard,
            Executor executor
    ) {
        this.artifactRepository = artifactRepository;
        this.linkRepository = linkRepository;
        this.specRevisionLookup = specRevisionLookup;
        this.projectSeedCatalog = projectSeedCatalog;
        this.coverageService = coverageService;
        this.accessGuard = accessGuard;
        this.executor = executor;
    }

    public TraceabilityAggregateDto loadAggregate(String workspaceId) {
        accessGuard.requireWorkspaceRead(workspaceId);
        CompletableFuture<SectionResultDto<List<TraceabilityMatrixRowDto>>> matrix = loadAsync(() -> loadMatrix(workspaceId), "matrix");
        CompletableFuture<SectionResultDto<TraceabilitySummaryDto>> summary = loadAsync(() -> loadSummary(workspaceId), "summary");
        CompletableFuture<SectionResultDto<List<TraceabilityGapDto>>> gaps = loadAsync(() -> loadGaps(workspaceId), "gaps");

        CompletableFuture.allOf(matrix, summary, gaps)
                .completeOnTimeout(null, DesignManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();
        return new TraceabilityAggregateDto(matrix.join(), summary.join(), gaps.join());
    }

    public List<TraceabilityMatrixRowDto> loadMatrix(String workspaceId) {
        List<SpecRevisionInfo> specs = specRevisionLookup.listByWorkspace(workspaceId);
        List<DesignArtifactEntity> artifacts = artifactRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        Map<String, DesignArtifactEntity> artifactsById = artifacts.stream()
                .collect(Collectors.toMap(DesignArtifactEntity::getId, artifact -> artifact, (left, right) -> left, LinkedHashMap::new));
        Map<String, List<DesignSpecLinkEntity>> linksBySpecId = linkRepository.findByArtifactIdIn(artifactsById.keySet()).stream()
                .collect(Collectors.groupingBy(DesignSpecLinkEntity::getSpecId, LinkedHashMap::new, Collectors.toList()));

        return specs.stream()
                .map(spec -> {
                    List<TraceabilityCellDto> cells = linksBySpecId.getOrDefault(spec.specId(), List.of()).stream()
                            .map(link -> {
                                DesignArtifactEntity artifact = artifactsById.get(link.getArtifactId());
                                CoverageStatus coverage = coverageService.compute(link.getDeclaredCoverage(), link.getCoversRevision(), spec);
                                return new TraceabilityCellDto(
                                        artifact.getId(),
                                        artifact.getTitle(),
                                        artifact.getCurrentVersionId(),
                                        coverage.name(),
                                        artifact.getLifecycle(),
                                        "/design-management/artifacts/" + artifact.getId()
                                );
                            })
                            .toList();
                    CoverageStatus overall = coverageService.worstStatus(cells.stream()
                            .map(cell -> CoverageStatus.valueOf(cell.coverageStatus()))
                            .toList());
                    return new TraceabilityMatrixRowDto(
                            spec.specId(),
                            spec.title(),
                            spec.requirementId(),
                            "/requirements/" + spec.requirementId(),
                            spec.projectId(),
                            projectSeedCatalog.project(spec.projectId()).name(),
                            spec.latestRevision(),
                            spec.state(),
                            cells,
                            cells.isEmpty() ? CoverageStatus.MISSING.name() : overall.name()
                    );
                })
                .sorted(Comparator.comparingInt((TraceabilityMatrixRowDto row) -> weight(row.overallCoverageStatus())).reversed()
                        .thenComparing(TraceabilityMatrixRowDto::specId))
                .toList();
    }

    public TraceabilitySummaryDto loadSummary(String workspaceId) {
        List<TraceabilityMatrixRowDto> rows = loadMatrix(workspaceId);
        Map<String, Long> buckets = rows.stream()
                .collect(Collectors.groupingBy(TraceabilityMatrixRowDto::overallCoverageStatus, LinkedHashMap::new, Collectors.counting()));
        int total = rows.size();
        List<CoverageBucketDto> bucketDtos = buckets.entrySet().stream()
                .map(entry -> new CoverageBucketDto(
                        entry.getKey(),
                        entry.getValue(),
                        total == 0 ? 0 : (entry.getValue() * 100.0) / total
                ))
                .toList();
        return new TraceabilitySummaryDto(rows.size(), artifactRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).size(), bucketDtos);
    }

    public List<TraceabilityGapDto> loadGaps(String workspaceId) {
        return loadMatrix(workspaceId).stream()
                .filter(row -> row.cells().isEmpty())
                .map(row -> new TraceabilityGapDto(
                        row.specId(),
                        row.specTitle(),
                        row.requirementId(),
                        row.requirementRoute(),
                        row.projectId(),
                        row.projectName(),
                        row.latestRevision(),
                        row.specState()
                ))
                .toList();
    }

    private int weight(String status) {
        return switch (status) {
            case "MISSING" -> 5;
            case "STALE" -> 4;
            case "PARTIAL" -> 3;
            case "UNKNOWN" -> 2;
            default -> 1;
        };
    }

    private <T> CompletableFuture<SectionResultDto<T>> loadAsync(Supplier<T> supplier, String label) {
        return CompletableFuture.supplyAsync(supplier, executor)
                .thenApply(SectionResultDto::ok)
                .completeOnTimeout(
                        SectionResultDto.fail(label + " projection timed out"),
                        DesignManagementConstants.PROJECTION_BUDGET.toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(ex -> SectionResultDto.fail(label + " projection failed: " + rootCause(ex).getMessage()));
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
