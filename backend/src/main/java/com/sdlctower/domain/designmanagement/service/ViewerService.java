package com.sdlctower.domain.designmanagement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.designmanagement.DesignManagementConstants;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.AiSummaryPayloadDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ArtifactHeaderDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ArtifactVersionRefDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ChangeLogEntryDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.LinkedSpecRowDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MemberRefDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.ViewerAggregateDto;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactAuthorRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkRepository;
import com.sdlctower.domain.designmanagement.policy.DesignAccessGuard;
import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewerService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignArtifactVersionRepository versionRepository;
    private final DesignArtifactAuthorRepository authorRepository;
    private final DesignSpecLinkRepository linkRepository;
    private final DesignAiSummaryRepository summaryRepository;
    private final DesignChangeLogRepository changeLogRepository;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final DesignAccessGuard accessGuard;
    private final CoverageService coverageService;
    private final SpecRevisionLookup specRevisionLookup;
    private final ObjectMapper objectMapper;
    private final Executor executor;

    @Autowired
    public ViewerService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactVersionRepository versionRepository,
            DesignArtifactAuthorRepository authorRepository,
            DesignSpecLinkRepository linkRepository,
            DesignAiSummaryRepository summaryRepository,
            DesignChangeLogRepository changeLogRepository,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            DesignAccessGuard accessGuard,
            CoverageService coverageService,
            SpecRevisionLookup specRevisionLookup,
            ObjectMapper objectMapper
    ) {
        this(
                artifactRepository,
                versionRepository,
                authorRepository,
                linkRepository,
                summaryRepository,
                changeLogRepository,
                projectSeedCatalog,
                accessGuard,
                coverageService,
                specRevisionLookup,
                objectMapper,
                ForkJoinPool.commonPool()
        );
    }

    ViewerService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactVersionRepository versionRepository,
            DesignArtifactAuthorRepository authorRepository,
            DesignSpecLinkRepository linkRepository,
            DesignAiSummaryRepository summaryRepository,
            DesignChangeLogRepository changeLogRepository,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            DesignAccessGuard accessGuard,
            CoverageService coverageService,
            SpecRevisionLookup specRevisionLookup,
            ObjectMapper objectMapper,
            Executor executor
    ) {
        this.artifactRepository = artifactRepository;
        this.versionRepository = versionRepository;
        this.authorRepository = authorRepository;
        this.linkRepository = linkRepository;
        this.summaryRepository = summaryRepository;
        this.changeLogRepository = changeLogRepository;
        this.projectSeedCatalog = projectSeedCatalog;
        this.accessGuard = accessGuard;
        this.coverageService = coverageService;
        this.specRevisionLookup = specRevisionLookup;
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    public ViewerAggregateDto loadAggregate(String artifactId, String versionId) {
        CompletableFuture<SectionResultDto<ArtifactHeaderDto>> header = loadAsync(() -> loadHeader(artifactId, versionId), "header");
        CompletableFuture<SectionResultDto<List<ArtifactVersionRefDto>>> versions = loadAsync(() -> loadVersions(artifactId), "versions");
        CompletableFuture<SectionResultDto<List<LinkedSpecRowDto>>> linkedSpecs = loadAsync(() -> loadLinkedSpecs(artifactId), "linkedSpecs");
        CompletableFuture<SectionResultDto<AiSummaryPayloadDto>> aiSummary = loadAsync(() -> loadAiSummary(artifactId, versionId), "aiSummary");
        CompletableFuture<SectionResultDto<List<ChangeLogEntryDto>>> changeLog = loadAsync(() -> loadChangeLog(artifactId), "changeLog");

        CompletableFuture.allOf(header, versions, linkedSpecs, aiSummary, changeLog)
                .completeOnTimeout(null, DesignManagementConstants.AGGREGATE_TOTAL_BUDGET.toMillis(), TimeUnit.MILLISECONDS)
                .join();

        return new ViewerAggregateDto(header.join(), versions.join(), linkedSpecs.join(), aiSummary.join(), changeLog.join());
    }

    public ArtifactHeaderDto loadHeader(String artifactId, String versionId) {
        DesignArtifactEntity artifact = resolveArtifact(artifactId);
        DesignArtifactVersionEntity version = resolveVersion(artifact, versionId);
        return new ArtifactHeaderDto(
                artifact.getId(),
                artifact.getWorkspaceId(),
                artifact.getProjectId(),
                projectSeedCatalog.project(artifact.getProjectId()).name(),
                artifact.getTitle(),
                artifact.getFormat(),
                artifact.getLifecycle(),
                authorRepository.findByIdArtifactId(artifactId).stream()
                        .map(author -> new MemberRefDto(author.getId().getMemberId(), projectSeedCatalog.memberDisplayName(author.getId().getMemberId())))
                        .toList(),
                version.getId(),
                version.getVersionNumber(),
                artifact.getCreatedAt(),
                new MemberRefDto(artifact.getRegisteredByMemberId(), projectSeedCatalog.memberDisplayName(artifact.getRegisteredByMemberId())),
                artifact.getUpdatedAt(),
                "/api/v1/design-management/artifacts/" + artifactId + "/raw?version=" + version.getId()
        );
    }

    public List<ArtifactVersionRefDto> loadVersions(String artifactId) {
        DesignArtifactEntity artifact = resolveArtifact(artifactId);
        return versionRepository.findByArtifactIdOrderByVersionNumberDesc(artifactId).stream()
                .map(version -> new ArtifactVersionRefDto(
                        version.getId(),
                        version.getVersionNumber(),
                        "v" + version.getVersionNumber(),
                        version.getHtmlSizeBytes(),
                        version.getChangelogNote(),
                        new MemberRefDto(version.getCreatedByMemberId(), projectSeedCatalog.memberDisplayName(version.getCreatedByMemberId())),
                        version.getCreatedAt(),
                        version.getId().equals(artifact.getCurrentVersionId())
                ))
                .toList();
    }

    public List<LinkedSpecRowDto> loadLinkedSpecs(String artifactId) {
        resolveArtifact(artifactId);
        List<DesignSpecLinkEntity> links = linkRepository.findByArtifactId(artifactId);
        Map<String, SpecRevisionInfo> specs = specRevisionLookup.findByIds(links.stream().map(DesignSpecLinkEntity::getSpecId).toList());
        return links.stream()
                .map(link -> {
                    SpecRevisionInfo spec = specs.get(link.getSpecId());
                    return new LinkedSpecRowDto(
                            link.getId(),
                            link.getSpecId(),
                            spec == null ? null : spec.requirementId(),
                            spec == null ? null : "/requirements/" + spec.requirementId(),
                            spec == null ? link.getSpecId() : spec.title(),
                            spec == null ? "UNKNOWN" : spec.state(),
                            link.getCoversRevision(),
                            spec == null ? 0 : spec.latestRevision(),
                            link.getDeclaredCoverage(),
                            coverageService.compute(link.getDeclaredCoverage(), link.getCoversRevision(), spec).name(),
                            new MemberRefDto(link.getLinkedByMemberId(), projectSeedCatalog.memberDisplayName(link.getLinkedByMemberId())),
                            link.getLinkedAt(),
                            coverageService.explain(link.getDeclaredCoverage(), link.getCoversRevision(), spec)
                    );
                })
                .toList();
    }

    public AiSummaryPayloadDto loadAiSummary(String artifactId, String versionId) {
        DesignArtifactEntity artifact = resolveArtifact(artifactId);
        DesignArtifactVersionEntity version = resolveVersion(artifact, versionId);
        DesignAiSummaryEntity summary = summaryRepository.findTopByArtifactIdAndVersionIdOrderByGeneratedAtDesc(artifactId, version.getId())
                .orElse(null);
        if (summary == null) {
            return new AiSummaryPayloadDto(null, artifactId, version.getId(), DesignManagementConstants.DEFAULT_SKILL_VERSION, "PENDING", null, List.of(), null, null);
        }

        try {
            Map<String, Object> payload = summary.getPayloadJson() == null
                    ? Map.of()
                    : objectMapper.readValue(summary.getPayloadJson(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> error = summary.getErrorJson() == null
                    ? Map.of()
                    : objectMapper.readValue(summary.getErrorJson(), new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            List<String> keyElements = payload.get("keyElements") instanceof List<?> raw
                    ? raw.stream().map(String::valueOf).toList()
                    : List.of();
            return new AiSummaryPayloadDto(
                    summary.getId(),
                    artifactId,
                    version.getId(),
                    summary.getSkillVersion(),
                    summary.getStatus(),
                    (String) payload.get("summaryText"),
                    keyElements,
                    (String) error.get("message"),
                    summary.getGeneratedAt()
            );
        } catch (Exception ex) {
            throw new UncheckedIOException(new java.io.IOException("Unable to decode AI summary payload", ex));
        }
    }

    public List<ChangeLogEntryDto> loadChangeLog(String artifactId) {
        resolveArtifact(artifactId);
        List<DesignChangeLogEntity> entries = changeLogRepository.findByArtifactIdOrderByOccurredAtDesc(artifactId);
        return entries.stream()
                .map(entry -> new ChangeLogEntryDto(
                        entry.getId(),
                        entry.getEntryType(),
                        entry.getActorMemberId(),
                        entry.getActorMemberId() == null ? "AI" : projectSeedCatalog.memberDisplayName(entry.getActorMemberId()),
                        entry.getReason(),
                        entry.getBeforeJson(),
                        entry.getAfterJson(),
                        entry.getOccurredAt()
                ))
                .toList();
    }

    public String loadRawPayload(String artifactId, String versionId) {
        DesignArtifactEntity artifact = resolveArtifact(artifactId);
        if ("RETIRED".equals(artifact.getLifecycle()) && !accessGuard.currentActor().isElevated()) {
            throw DesignManagementException.forbidden("DM_ROLE_REQUIRED", "Retired artifacts require admin visibility: " + artifactId);
        }
        return resolveVersion(artifact, versionId).getHtmlPayload();
    }

    private DesignArtifactEntity resolveArtifact(String artifactId) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireRead(artifact.getProjectId());
        return artifact;
    }

    private DesignArtifactVersionEntity resolveVersion(DesignArtifactEntity artifact, String versionId) {
        String resolvedVersionId = versionId == null || versionId.isBlank() ? artifact.getCurrentVersionId() : versionId;
        return versionRepository.findByIdAndArtifactId(resolvedVersionId, artifact.getId())
                .orElseThrow(() -> DesignManagementException.notFound("DM_VERSION_NOT_FOUND", "Version not found: " + resolvedVersionId));
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
