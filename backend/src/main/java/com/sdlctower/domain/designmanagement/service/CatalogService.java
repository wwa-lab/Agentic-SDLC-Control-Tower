package com.sdlctower.domain.designmanagement.service;

import com.sdlctower.domain.designmanagement.DesignManagementConstants;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogAggregateDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogArtifactRowDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogFiltersDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogSectionDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.CatalogSummaryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MemberRefDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.CoverageStatus;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactAuthorEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactAuthorRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkRepository;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
public class CatalogService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignArtifactAuthorRepository authorRepository;
    private final DesignSpecLinkRepository linkRepository;
    private final DesignAiSummaryRepository summaryRepository;
    private final SpecRevisionLookup specRevisionLookup;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final CoverageService coverageService;
    private final Executor executor;

    @Autowired
    public CatalogService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactAuthorRepository authorRepository,
            DesignSpecLinkRepository linkRepository,
            DesignAiSummaryRepository summaryRepository,
            SpecRevisionLookup specRevisionLookup,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CoverageService coverageService
    ) {
        this(
                artifactRepository,
                authorRepository,
                linkRepository,
                summaryRepository,
                specRevisionLookup,
                projectSeedCatalog,
                coverageService,
                ForkJoinPool.commonPool()
        );
    }

    CatalogService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactAuthorRepository authorRepository,
            DesignSpecLinkRepository linkRepository,
            DesignAiSummaryRepository summaryRepository,
            SpecRevisionLookup specRevisionLookup,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            CoverageService coverageService,
            Executor executor
    ) {
        this.artifactRepository = artifactRepository;
        this.authorRepository = authorRepository;
        this.linkRepository = linkRepository;
        this.summaryRepository = summaryRepository;
        this.specRevisionLookup = specRevisionLookup;
        this.projectSeedCatalog = projectSeedCatalog;
        this.coverageService = coverageService;
        this.executor = executor;
    }

    public CatalogAggregateDto loadAggregate(String workspaceId) {
        CompletableFuture<SectionResultDto<CatalogSummaryDto>> summary = loadAsync(() -> loadSummary(workspaceId), "summary");
        CompletableFuture<SectionResultDto<List<CatalogSectionDto>>> grid = loadAsync(() -> loadGrid(workspaceId), "grid");
        CompletableFuture<SectionResultDto<CatalogFiltersDto>> filters = loadAsync(() -> loadFilters(workspaceId), "filters");

        CompletableFuture.allOf(summary, grid, filters)
                .completeOnTimeout(null, DesignManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new CatalogAggregateDto(summary.join(), grid.join(), filters.join());
    }

    public CatalogSummaryDto loadSummary(String workspaceId) {
        List<CatalogArtifactRowDto> rows = flatten(loadGrid(workspaceId));
        Map<String, Long> coverageBuckets = rows.stream()
                .collect(Collectors.groupingBy(CatalogArtifactRowDto::worstCoverageStatus, LinkedHashMap::new, Collectors.counting()));
        long linkedArtifacts = rows.stream().filter(row -> row.linkedSpecCount() > 0).count();
        long draftArtifacts = rows.stream().filter(row -> "DRAFT".equals(row.lifecycle())).count();
        long publishedArtifacts = rows.stream().filter(row -> "PUBLISHED".equals(row.lifecycle())).count();
        long retiredArtifacts = rows.stream().filter(row -> "RETIRED".equals(row.lifecycle())).count();
        long missing = coverageBuckets.getOrDefault(CoverageStatus.MISSING.name(), 0L);
        long stale = coverageBuckets.getOrDefault(CoverageStatus.STALE.name(), 0L);

        return new CatalogSummaryDto(
                workspaceId,
                rows.size(),
                linkedArtifacts,
                draftArtifacts,
                publishedArtifacts,
                retiredArtifacts,
                coverageBuckets,
                Instant.now(),
                missing + " artifacts need spec linkage attention, " + stale + " are stale against the latest spec revisions."
        );
    }

    public List<CatalogSectionDto> loadGrid(String workspaceId) {
        List<DesignArtifactEntity> artifacts = artifactRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        Map<String, List<MemberRefDto>> authorsByArtifact = loadAuthors(artifacts.stream().map(DesignArtifactEntity::getId).toList());
        Map<String, List<DesignSpecLinkEntity>> linksByArtifact = linkRepository.findByArtifactIdIn(artifacts.stream().map(DesignArtifactEntity::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(DesignSpecLinkEntity::getArtifactId, LinkedHashMap::new, Collectors.toList()));
        Map<String, SpecRevisionInfo> specs = specRevisionLookup.findByIds(linksByArtifact.values().stream()
                .flatMap(Collection::stream)
                .map(DesignSpecLinkEntity::getSpecId)
                .toList());
        Map<String, List<DesignAiSummaryEntity>> summariesByArtifact = summaryRepository.findByArtifactIdIn(artifacts.stream().map(DesignArtifactEntity::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(DesignAiSummaryEntity::getArtifactId, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<CatalogArtifactRowDto>> grouped = new LinkedHashMap<>();
        for (DesignArtifactEntity artifact : artifacts) {
            List<DesignSpecLinkEntity> links = linksByArtifact.getOrDefault(artifact.getId(), List.of());
            List<CoverageStatus> statuses = links.stream()
                    .map(link -> coverageService.compute(link.getDeclaredCoverage(), link.getCoversRevision(), specs.get(link.getSpecId())))
                    .toList();
            CoverageStatus worst = coverageService.worstStatus(statuses);
            boolean aiReady = summariesByArtifact.getOrDefault(artifact.getId(), List.of()).stream()
                    .anyMatch(summary -> summary.getVersionId().equals(artifact.getCurrentVersionId()) && "SUCCESS".equals(summary.getStatus()));

            CatalogArtifactRowDto row = new CatalogArtifactRowDto(
                    artifact.getId(),
                    artifact.getWorkspaceId(),
                    artifact.getProjectId(),
                    projectSeedCatalog.project(artifact.getProjectId()).name(),
                    artifact.getTitle(),
                    artifact.getFormat(),
                    artifact.getLifecycle(),
                    authorsByArtifact.getOrDefault(artifact.getId(), List.of()),
                    artifact.getCurrentVersionId(),
                    currentVersionNumber(artifact.getCurrentVersionId()),
                    artifact.getUpdatedAt(),
                    links.size(),
                    worst.name(),
                    aiReady
            );
            grouped.computeIfAbsent(artifact.getProjectId(), key -> new ArrayList<>()).add(row);
        }

        return grouped.entrySet().stream()
                .map(entry -> new CatalogSectionDto(
                        entry.getKey(),
                        projectSeedCatalog.project(entry.getKey()).name(),
                        entry.getValue()
                ))
                .toList();
    }

    public CatalogFiltersDto loadFilters(String workspaceId) {
        List<DesignArtifactEntity> artifacts = artifactRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        return new CatalogFiltersDto(
                artifacts.stream().map(DesignArtifactEntity::getProjectId).distinct().toList(),
                artifacts.stream().map(DesignArtifactEntity::getLifecycle).distinct().sorted().toList(),
                List.of("OK", "PARTIAL", "STALE", "MISSING", "UNKNOWN")
        );
    }

    private Map<String, List<MemberRefDto>> loadAuthors(List<String> artifactIds) {
        return authorRepository.findByIdArtifactIdIn(artifactIds).stream()
                .collect(Collectors.groupingBy(
                        entity -> entity.getId().getArtifactId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                entity -> new MemberRefDto(
                                        entity.getId().getMemberId(),
                                        projectSeedCatalog.memberDisplayName(entity.getId().getMemberId())
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private List<CatalogArtifactRowDto> flatten(List<CatalogSectionDto> sections) {
        return sections.stream().flatMap(section -> section.artifacts().stream()).toList();
    }

    private int currentVersionNumber(String currentVersionId) {
        int marker = currentVersionId == null ? -1 : currentVersionId.lastIndexOf("-v");
        if (marker < 0) {
            return 0;
        }
        return Integer.parseInt(currentVersionId.substring(marker + 2));
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
