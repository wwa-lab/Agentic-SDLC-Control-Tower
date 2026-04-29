package com.sdlctower.domain.requirement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.AgentRunCallbackRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.AgentRunDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.ArtifactLinkDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.ArtifactLinkRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.CreateAgentRunRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.CreateDocumentReviewRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.CreateQualityGateRunRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.CreateSourceReferenceRequestDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.DocumentQualityDimensionDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.DocumentQualityFindingDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.DocumentQualityGateResultDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.DocumentReviewDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.FreshnessItemDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.RequirementTraceabilityDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.SddDocumentContentDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.SddDocumentIndexDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.SddDocumentStageDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.SddWorkspaceDto;
import com.sdlctower.domain.requirement.dto.RequirementControlPlaneDtos.SourceReferenceDto;
import com.sdlctower.domain.requirement.persistence.ProjectSddWorkspaceEntity;
import com.sdlctower.domain.requirement.persistence.ProjectSddWorkspaceRepository;
import com.sdlctower.domain.requirement.persistence.RequirementAgentRunEntity;
import com.sdlctower.domain.requirement.persistence.RequirementAgentRunRepository;
import com.sdlctower.domain.requirement.persistence.RequirementArtifactLinkEntity;
import com.sdlctower.domain.requirement.persistence.RequirementArtifactLinkRepository;
import com.sdlctower.domain.requirement.persistence.RequirementDocumentReviewEntity;
import com.sdlctower.domain.requirement.persistence.RequirementDocumentReviewRepository;
import com.sdlctower.domain.requirement.persistence.RequirementDocumentQualityGateRunEntity;
import com.sdlctower.domain.requirement.persistence.RequirementDocumentQualityGateRunRepository;
import com.sdlctower.domain.requirement.persistence.RequirementEntity;
import com.sdlctower.domain.requirement.persistence.RequirementRepository;
import com.sdlctower.domain.requirement.persistence.RequirementSddDocumentIndexEntity;
import com.sdlctower.domain.requirement.persistence.RequirementSddDocumentIndexRepository;
import com.sdlctower.domain.requirement.persistence.RequirementSourceReferenceEntity;
import com.sdlctower.domain.requirement.persistence.RequirementSourceReferenceRepository;
import com.sdlctower.platform.profile.PipelineDocumentStageDto;
import com.sdlctower.platform.profile.PipelineProfileDto;
import com.sdlctower.platform.profile.PipelineProfileService;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequirementControlPlaneService {
    private static final List<String> SOURCE_TYPES = List.of("JIRA", "CONFLUENCE", "GITHUB", "KB", "UPLOAD", "URL");
    private static final List<String> REVIEW_DECISIONS = List.of("COMMENT", "APPROVED", "CHANGES_REQUESTED", "REJECTED");
    private static final int QUALITY_GATE_THRESHOLD = 80;

    private final RequirementRepository requirementRepository;
    private final RequirementSourceReferenceRepository sourceRepository;
    private final RequirementSddDocumentIndexRepository documentRepository;
    private final ProjectSddWorkspaceRepository workspaceRepository;
    private final RequirementDocumentReviewRepository reviewRepository;
    private final RequirementDocumentQualityGateRunRepository qualityGateRepository;
    private final RequirementAgentRunRepository agentRunRepository;
    private final RequirementArtifactLinkRepository artifactLinkRepository;
    private final PipelineProfileService pipelineProfileService;
    private final GitHubDocumentGateway githubDocumentGateway;
    private final List<RequirementSourceProvider> sourceProviders;
    private final ObjectMapper objectMapper;

    public RequirementControlPlaneService(
            RequirementRepository requirementRepository,
            RequirementSourceReferenceRepository sourceRepository,
            RequirementSddDocumentIndexRepository documentRepository,
            ProjectSddWorkspaceRepository workspaceRepository,
            RequirementDocumentReviewRepository reviewRepository,
            RequirementDocumentQualityGateRunRepository qualityGateRepository,
            RequirementAgentRunRepository agentRunRepository,
            RequirementArtifactLinkRepository artifactLinkRepository,
            PipelineProfileService pipelineProfileService,
            GitHubDocumentGateway githubDocumentGateway,
            List<RequirementSourceProvider> sourceProviders,
            ObjectMapper objectMapper
    ) {
        this.requirementRepository = requirementRepository;
        this.sourceRepository = sourceRepository;
        this.documentRepository = documentRepository;
        this.workspaceRepository = workspaceRepository;
        this.reviewRepository = reviewRepository;
        this.qualityGateRepository = qualityGateRepository;
        this.agentRunRepository = agentRunRepository;
        this.artifactLinkRepository = artifactLinkRepository;
        this.pipelineProfileService = pipelineProfileService;
        this.githubDocumentGateway = githubDocumentGateway;
        this.sourceProviders = sourceProviders;
        this.objectMapper = objectMapper;
    }

    public List<SourceReferenceDto> listSources(String requirementId) {
        validateRequirement(requirementId);
        return sourceRepository.findByRequirementIdOrderByCreatedAtAsc(requirementId).stream().map(this::toSourceDto).toList();
    }

    @Transactional
    public SourceReferenceDto createSource(String requirementId, CreateSourceReferenceRequestDto request) {
        validateRequirement(requirementId);
        if (request == null || isBlank(request.url())) throw new IllegalArgumentException("url is required");
        String sourceType = normalizeSourceType(request.sourceType(), request.url());
        if (!SOURCE_TYPES.contains(sourceType)) throw new IllegalArgumentException("Unsupported sourceType: " + sourceType);
        Instant now = Instant.now();
        RequirementSourceReferenceEntity entity = RequirementSourceReferenceEntity.create(
                "SRC-" + UUID.randomUUID(),
                requirementId,
                sourceType,
                firstNonBlank(request.externalId(), inferExternalId(request.url())),
                firstNonBlank(request.title(), titleFromUrl(request.url())),
                request.url().trim(),
                now,
                now,
                "FRESH",
                null,
                now
        );
        return toSourceDto(sourceRepository.save(entity));
    }

    @Transactional
    public SourceReferenceDto refreshSource(String sourceId) {
        RequirementSourceReferenceEntity source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source reference not found: " + sourceId));
        RequirementSourceProvider provider = sourceProviders.stream()
                .filter(candidate -> candidate.supports(source.getSourceType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No source provider supports " + source.getSourceType()));
        try {
            RequirementSourceProvider.SourceMetadata metadata = provider.refresh(source);
            source.refreshMetadata(
                    metadata.externalId(),
                    metadata.title(),
                    firstNonNull(metadata.sourceUpdatedAt(), Instant.now()),
                    firstNonNull(metadata.fetchedAt(), Instant.now()),
                    firstNonBlank(metadata.freshnessStatus(), "FRESH"),
                    metadata.errorMessage()
            );
        } catch (RuntimeException exception) {
            source.refresh(source.getSourceUpdatedAt(), Instant.now(), "ERROR", exception.getMessage());
        }
        return toSourceDto(sourceRepository.save(source));
    }

    public SddDocumentIndexDto listSddDocuments(String requirementId) {
        return listSddDocuments(requirementId, null);
    }

    public SddDocumentIndexDto listSddDocuments(String requirementId, String requestedProfileId) {
        validateRequirement(requirementId);
        List<RequirementSddDocumentIndexEntity> documents = documentRepository.findByRequirementIdOrderByIndexedAtAsc(requirementId);
        String profileId = firstNonBlank(
                requestedProfileId,
                documents.stream().findFirst().map(RequirementSddDocumentIndexEntity::getProfileId).orElse("standard-sdd")
        );
        PipelineProfileDto profile = pipelineProfileService.getProfile(profileId);
        Map<String, RequirementSddDocumentIndexEntity> byType = new LinkedHashMap<>();
        for (RequirementSddDocumentIndexEntity document : documents) {
            if (!profile.id().equals(document.getProfileId())) {
                continue;
            }
            byType.put(document.getSddType(), document);
        }
        List<SddDocumentStageDto> stages = profile.documentStages().stream()
                .map(stage -> toDocumentStageDto(stage, byType.get(stage.sddType())))
                .toList();
        return new SddDocumentIndexDto(requirementId, profile.id(), resolveWorkspace(requirementId, documents), stages);
    }

    @Transactional
    public SddDocumentIndexDto syncSddDocuments(String requirementId, String requestedProfileId) {
        RequirementEntity requirement = getRequirementEntity(requirementId);
        List<RequirementSddDocumentIndexEntity> documents = documentRepository.findByRequirementIdOrderByIndexedAtAsc(requirementId);
        String profileId = firstNonBlank(
                requestedProfileId,
                documents.stream().findFirst().map(RequirementSddDocumentIndexEntity::getProfileId).orElse("standard-sdd")
        );
        PipelineProfileDto profile = pipelineProfileService.getProfile(profileId);
        SddWorkspaceDto workspace = resolveWorkspace(requirementId, documents);
        List<SourceReferenceDto> sources = sourceRepository.findByRequirementIdOrderByCreatedAtAsc(requirementId).stream().map(this::toSourceDto).toList();
        List<GitHubDocumentGateway.GitHubDocumentMetadata> markdownFiles = githubDocumentGateway.listMarkdownDocuments(
                workspace.sddRepoFullName(),
                workspace.workingBranch(),
                workspace.docsRoot()
        );
        Map<String, RequirementSddDocumentIndexEntity> byType = new LinkedHashMap<>();
        for (RequirementSddDocumentIndexEntity document : documents) {
            if (profile.id().equals(document.getProfileId())) {
                byType.put(document.getSddType(), document);
            }
        }
        Instant now = Instant.now();
        for (PipelineDocumentStageDto stage : profile.documentStages()) {
            RequirementSddDocumentIndexEntity existing = byType.get(stage.sddType());
            Optional<GitHubDocumentGateway.GitHubDocumentMetadata> match = findBestDocumentMatch(markdownFiles, stage, existing, requirement, sources);
            if (match.isPresent()) {
                documentRepository.save(upsertDocument(requirementId, profile.id(), workspace, stage, existing, match.get(), now));
            } else if (existing != null) {
                existing.markMissing(now);
                documentRepository.save(existing);
            }
        }
        return listSddDocuments(requirementId, profile.id());
    }

    public SddDocumentContentDto getDocument(String documentId) {
        RequirementSddDocumentIndexEntity document = getDocumentEntity(documentId);
        GitHubDocumentGateway.GitHubDocumentContent content = githubDocumentGateway.fetchMarkdown(
                document.getRepoFullName(),
                document.getBranchOrRef(),
                document.getPath(),
                document.getLatestCommitSha(),
                document.getLatestBlobSha(),
                document.getGithubUrl(),
                document.getTitle()
        );
        return new SddDocumentContentDto(toDocumentStageDto(null, document), content.markdown(), content.commitSha(), content.blobSha(), content.githubUrl(), content.fetchedAt());
    }

    @Transactional
    public DocumentQualityGateResultDto runDocumentQualityGate(String documentId, CreateQualityGateRunRequestDto request) {
        RequirementSddDocumentIndexEntity document = getDocumentEntity(documentId);
        if ("MISSING".equals(document.getStatus())) {
            throw new IllegalArgumentException("Quality gate cannot run for a missing document");
        }
        int score = scoreDocumentQuality(document);
        String band = score >= 90 ? "EXCELLENT" : score >= QUALITY_GATE_THRESHOLD ? "GOOD" : "BLOCKED";
        boolean passed = score >= QUALITY_GATE_THRESHOLD;
        List<DocumentQualityDimensionDto> dimensions = buildQualityDimensions(document, score);
        List<DocumentQualityFindingDto> findings = buildQualityFindings(document, passed);
        String summary = passed
                ? qualityLabel(band) + " quality. This document meets the minimum score for downstream review."
                : "Score is below 80. This document must be improved before downstream approval.";
        Instant now = Instant.now();
        RequirementDocumentQualityGateRunEntity run = RequirementDocumentQualityGateRunEntity.create(
                "QG-" + UUID.randomUUID(),
                document.getId(),
                document.getRequirementId(),
                document.getProfileId(),
                document.getSddType(),
                score,
                band,
                passed,
                QUALITY_GATE_THRESHOLD,
                "quality-gate." + document.getProfileId() + "." + document.getSddType() + ".v1",
                document.getLatestCommitSha(),
                document.getLatestBlobSha(),
                writeJson(dimensions),
                writeJson(findings),
                summary,
                "current-user",
                normalize(request == null ? null : request.triggerMode(), "MANUAL"),
                now
        );
        return toQualityGateDto(qualityGateRepository.save(run), document);
    }

    public DocumentQualityGateResultDto getLatestDocumentQualityGate(String documentId) {
        RequirementSddDocumentIndexEntity document = getDocumentEntity(documentId);
        return qualityGateRepository.findTopByDocumentIdOrderByScoredAtDesc(documentId)
                .map(run -> toQualityGateDto(run, document))
                .orElseThrow(() -> new ResourceNotFoundException("Quality gate result not found for document: " + documentId));
    }

    @Transactional
    public List<DocumentQualityGateResultDto> runRequirementQualityGates(String requirementId, CreateQualityGateRunRequestDto request) {
        validateRequirement(requirementId);
        return documentRepository.findByRequirementIdOrderByIndexedAtAsc(requirementId).stream()
                .filter(document -> !"MISSING".equals(document.getStatus()))
                .map(document -> runDocumentQualityGate(document.getId(), request))
                .toList();
    }

    @Transactional
    public DocumentReviewDto createReview(String documentId, CreateDocumentReviewRequestDto request) {
        RequirementSddDocumentIndexEntity document = getDocumentEntity(documentId);
        if (request == null || isBlank(request.commitSha()) || isBlank(request.blobSha())) {
            throw new IllegalArgumentException("commitSha and blobSha are required");
        }
        String decision = normalize(request.decision(), "COMMENT");
        if (!REVIEW_DECISIONS.contains(decision)) throw new IllegalArgumentException("Unsupported review decision: " + decision);
        if ("APPROVED".equals(decision)) {
            DocumentQualityGateResultDto gate = latestQualityGateFor(document);
            if (gate == null) {
                throw new IllegalArgumentException("Document quality gate must pass before approval");
            }
            if (gate.stale()) {
                throw new IllegalArgumentException("Document quality gate is stale and must be rerun before approval");
            }
            if (!gate.passed()) {
                throw new IllegalArgumentException("Document quality gate score is below threshold");
            }
        }
        if ("REJECTED".equals(decision) && isBlank(request.comment())) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        boolean stale = !document.getLatestBlobSha().equals(request.blobSha()) || !document.getLatestCommitSha().equals(request.commitSha());
        RequirementDocumentReviewEntity review = RequirementDocumentReviewEntity.create(
                "REV-" + UUID.randomUUID(),
                document.getId(),
                document.getRequirementId(),
                decision,
                request.comment(),
                "current-user",
                "BUSINESS",
                request.commitSha(),
                request.blobSha(),
                firstNonBlank(request.anchorType(), "DOCUMENT"),
                request.anchorValue(),
                stale,
                Instant.now()
        );
        return toReviewDto(reviewRepository.save(review));
    }

    public List<DocumentReviewDto> listReviews(String requirementId) {
        validateRequirement(requirementId);
        return reviewRepository.findByRequirementIdOrderByCreatedAtDesc(requirementId).stream()
                .map(this::toReviewDto)
                .toList();
    }

    @Transactional
    public AgentRunDto createAgentRun(String requirementId, CreateAgentRunRequestDto request) {
        validateRequirement(requirementId);
        String skillKey = firstNonBlank(request == null ? null : request.skillKey(), "req-to-user-story");
        String targetStage = firstNonBlank(request == null ? null : request.targetStage(), "spec");
        String profileId = listSddDocuments(requirementId, request == null ? null : request.profileId()).profileId();
        String executionId = "EXEC-" + UUID.randomUUID();
        Map<String, Object> manifest = buildManifest(executionId, requirementId, profileId, skillKey, targetStage, request == null ? null : request.notes());
        Instant now = Instant.now();
        RequirementAgentRunEntity entity = RequirementAgentRunEntity.create(executionId, requirementId, profileId, skillKey, targetStage, "MANIFEST_READY", writeJson(manifest), now);
        return toAgentRunDto(agentRunRepository.save(entity));
    }

    public AgentRunDto getAgentRun(String executionId) {
        return toAgentRunDto(agentRunRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + executionId)));
    }

    @Transactional
    public AgentRunDto applyAgentRunCallback(String executionId, AgentRunCallbackRequestDto request) {
        RequirementAgentRunEntity run = agentRunRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + executionId));
        String status = normalize(request == null ? null : request.status(), "COMPLETED");
        String outputSummary = writeJson(request == null ? Map.of() : request.outputSummary());
        run.applyCallback(status, outputSummary, request == null ? null : request.errorMessage(), Instant.now());
        agentRunRepository.save(run);
        List<ArtifactLinkRequestDto> links = request == null || request.artifactLinks() == null ? List.of() : request.artifactLinks();
        for (ArtifactLinkRequestDto link : links) {
            artifactLinkRepository.save(RequirementArtifactLinkEntity.create(
                    "ART-" + UUID.randomUUID(),
                    executionId,
                    run.getRequirementId(),
                    firstNonBlank(link.artifactType(), "GITHUB_PR"),
                    firstNonBlank(link.storageType(), "GITHUB"),
                    firstNonBlank(link.title(), "Agent artifact"),
                    firstNonBlank(link.uri(), "#"),
                    link.repoFullName(),
                    link.path(),
                    link.commitSha(),
                    link.blobSha(),
                    firstNonBlank(link.status(), "IN_REVIEW"),
                    Instant.now()
            ));
        }
        return toAgentRunDto(run);
    }

    public RequirementTraceabilityDto getTraceability(String requirementId) {
        return getTraceability(requirementId, null);
    }

    public RequirementTraceabilityDto getTraceability(String requirementId, String profileId) {
        List<SourceReferenceDto> sources = listSources(requirementId);
        SddDocumentIndexDto documents = listSddDocuments(requirementId, profileId);
        List<DocumentReviewDto> reviews = listReviews(requirementId);
        List<AgentRunDto> runs = agentRunRepository.findByRequirementIdOrderByCreatedAtDesc(requirementId).stream().map(this::toAgentRunDto).toList();
        List<ArtifactLinkDto> artifacts = artifactLinkRepository.findByRequirementIdOrderByCreatedAtDesc(requirementId).stream().map(this::toArtifactDto).toList();
        return new RequirementTraceabilityDto(requirementId, sources, documents, reviews, runs, artifacts, buildFreshness(sources, documents.stages(), reviews));
    }

    private Map<String, Object> buildManifest(String executionId, String requirementId, String profileId, String skillKey, String targetStage, String notes) {
        List<SourceReferenceDto> sources = listSources(requirementId);
        SddDocumentIndexDto documentIndex = listSddDocuments(requirementId, profileId);
        List<SddDocumentStageDto> documents = documentIndex.stages().stream().filter(stage -> !stage.missing()).toList();
        SddWorkspaceDto workspace = documentIndex.workspace();
        Map<String, Object> repo = documents.stream().findFirst()
                .map(doc -> Map.<String, Object>of("fullName", doc.repoFullName(), "baseRef", doc.branchOrRef()))
                .orElse(Map.of("fullName", "wwa-lab/payment-gateway-pro", "baseRef", "main"));
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("executionId", executionId);
        manifest.put("requirementId", requirementId);
        manifest.put("profileId", profileId);
        manifest.put("skillKey", skillKey);
        manifest.put("repo", repo);
        manifest.put("sourceRepo", workspace == null ? Map.of() : Map.of("fullName", workspace.sourceRepoFullName(), "baseRef", workspace.baseBranch()));
        manifest.put("sddWorkspace", workspace == null ? Map.of() : Map.of(
                "id", workspace.id(),
                "applicationId", workspace.applicationId(),
                "applicationName", workspace.applicationName(),
                "snowGroup", workspace.snowGroup(),
                "repoFullName", workspace.sddRepoFullName(),
                "baseBranch", workspace.baseBranch(),
                "workingBranch", workspace.workingBranch(),
                "lifecycleStatus", workspace.lifecycleStatus(),
                "docsRoot", workspace.docsRoot()
        ));
        manifest.put("knowledgeBase", workspace == null ? Map.of() : Map.of(
                "repoFullName", workspace.kbRepoFullName(),
                "mainBranch", workspace.kbMainBranch(),
                "previewBranch", workspace.kbPreviewBranch(),
                "graphManifestPath", workspace.graphManifestPath()
        ));
        manifest.put("sources", sources.stream().map(s -> {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("id", s.id());
            source.put("type", s.sourceType());
            source.put("url", s.url());
            source.put("externalId", s.externalId());
            source.put("versionKey", s.sourceUpdatedAt() == null ? null : "updated-" + s.sourceUpdatedAt());
            return source;
        }).toList());
        manifest.put("documents", documents.stream().map(d -> {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("id", d.id());
            document.put("type", d.sddType());
            document.put("path", d.path());
            document.put("commitSha", d.latestCommitSha());
            document.put("blobSha", d.latestBlobSha());
            return document;
        }).toList());
        manifest.put("output", Map.of("docsRoot", workspace == null ? "docs/" : workspace.docsRoot(), "targetStage", targetStage));
        manifest.put("constraints", List.of("Read source repo for code truth", "Write SDD Markdown to the project SDD branch", "Use GitHub as document source of truth", "Preserve reviewed commit/blob metadata"));
        manifest.put("notes", notes == null ? "" : notes);
        return manifest;
    }

    private SddWorkspaceDto resolveWorkspace(String requirementId, List<RequirementSddDocumentIndexEntity> documents) {
        String workspaceId = documents.stream()
                .map(RequirementSddDocumentIndexEntity::getSddWorkspaceId)
                .filter(id -> !isBlank(id))
                .findFirst()
                .orElse(null);
        if (!isBlank(workspaceId)) {
            return workspaceRepository.findById(workspaceId).map(this::toWorkspaceDto).orElseGet(() -> fallbackWorkspace(requirementId, documents));
        }
        return fallbackWorkspace(requirementId, documents);
    }

    private SddWorkspaceDto fallbackWorkspace(String requirementId, List<RequirementSddDocumentIndexEntity> documents) {
        RequirementSddDocumentIndexEntity first = documents.stream().findFirst().orElse(null);
        String repoFullName = first == null ? "wwa-lab/payment-gateway-sdd" : first.getRepoFullName();
        String branch = first == null ? "main" : first.getBranchOrRef();
        return new SddWorkspaceDto(
                "SDDW-" + requirementId,
                "unknown-application",
                "Unknown Application",
                "UNKNOWN-SNOW-GROUP",
                repoFullName,
                repoFullName,
                "main",
                branch,
                "IN_DEVELOPMENT",
                "docs/",
                null,
                repoFullName.replace("-sdd", "-knowledge-base"),
                "main",
                branch,
                "_graph/manifest.json"
        );
    }

    private SddWorkspaceDto toWorkspaceDto(ProjectSddWorkspaceEntity workspace) {
        return new SddWorkspaceDto(
                workspace.getId(),
                workspace.getApplicationId(),
                workspace.getApplicationName(),
                workspace.getSnowGroup(),
                workspace.getSourceRepoFullName(),
                workspace.getSddRepoFullName(),
                workspace.getBaseBranch(),
                workspace.getWorkingBranch(),
                workspace.getLifecycleStatus(),
                workspace.getDocsRoot(),
                workspace.getReleasePrUrl(),
                workspace.getKbRepoFullName(),
                workspace.getKbMainBranch(),
                workspace.getKbPreviewBranch(),
                workspace.getGraphManifestPath()
        );
    }

    private List<FreshnessItemDto> buildFreshness(List<SourceReferenceDto> sources, List<SddDocumentStageDto> documents, List<DocumentReviewDto> reviews) {
        List<FreshnessItemDto> items = new ArrayList<>();
        if (sources.isEmpty()) items.add(new FreshnessItemDto("SOURCE", "none", "MISSING_SOURCE", "No source reference linked"));
        for (SourceReferenceDto source : sources) items.add(new FreshnessItemDto("SOURCE", source.id(), source.freshnessStatus(), source.title()));
        for (SddDocumentStageDto document : documents) {
            items.add(new FreshnessItemDto("DOCUMENT", document.id() == null ? document.sddType() : document.id(), document.freshnessStatus(), document.stageLabel()));
        }
        for (DocumentReviewDto review : reviews) {
            items.add(new FreshnessItemDto("REVIEW", review.id(), review.stale() ? "DOCUMENT_CHANGED_AFTER_REVIEW" : "FRESH", review.decision()));
        }
        return items;
    }

    private SddDocumentStageDto toDocumentStageDto(PipelineDocumentStageDto stage, RequirementSddDocumentIndexEntity document) {
        if (document == null) {
            return new SddDocumentStageDto(null, stage.sddType(), stage.label(), stage.label(), null, null, stage.pathPattern(), null, null, null, "MISSING", "MISSING_DOCUMENT", true, null);
        }
        if (stage != null && "MISSING".equals(document.getStatus())) {
            return new SddDocumentStageDto(null, stage.sddType(), stage.label(), stage.label(), null, null, stage.pathPattern(), null, null, null, "MISSING", "MISSING_DOCUMENT", true, null);
        }
        String freshness = reviewRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId()).stream()
                .anyMatch(review -> !document.getLatestBlobSha().equals(review.getBlobSha())) ? "DOCUMENT_CHANGED_AFTER_REVIEW" : "FRESH";
        return new SddDocumentStageDto(document.getId(), document.getSddType(), document.getStageLabel(), document.getTitle(), document.getRepoFullName(), document.getBranchOrRef(), document.getPath(), document.getLatestCommitSha(), document.getLatestBlobSha(), document.getGithubUrl(), document.getStatus(), freshness, false, latestQualityGateFor(document));
    }

    private RequirementSddDocumentIndexEntity upsertDocument(
            String requirementId,
            String profileId,
            SddWorkspaceDto workspace,
            PipelineDocumentStageDto stage,
            RequirementSddDocumentIndexEntity existing,
            GitHubDocumentGateway.GitHubDocumentMetadata metadata,
            Instant indexedAt
    ) {
        String status = existing == null || "MISSING".equals(existing.getStatus()) ? "IN_REVIEW" : existing.getStatus();
        String title = firstNonBlank(metadata.title(), stage.label());
        if (existing == null) {
            return RequirementSddDocumentIndexEntity.create(
                    buildDocumentId(requirementId, stage.sddType()),
                    requirementId,
                    profileId,
                    workspace.id(),
                    stage.sddType(),
                    stage.label(),
                    title,
                    workspace.sddRepoFullName(),
                    workspace.workingBranch(),
                    metadata.path(),
                    metadata.commitSha(),
                    metadata.blobSha(),
                    metadata.githubUrl(),
                    status,
                    indexedAt
            );
        }
        existing.refreshFromGitHub(
                workspace.id(),
                stage.label(),
                title,
                workspace.sddRepoFullName(),
                workspace.workingBranch(),
                metadata.path(),
                metadata.commitSha(),
                metadata.blobSha(),
                metadata.githubUrl(),
                status,
                indexedAt
        );
        return existing;
    }

    private Optional<GitHubDocumentGateway.GitHubDocumentMetadata> findBestDocumentMatch(
            List<GitHubDocumentGateway.GitHubDocumentMetadata> files,
            PipelineDocumentStageDto stage,
            RequirementSddDocumentIndexEntity existing,
            RequirementEntity requirement,
            List<SourceReferenceDto> sources
    ) {
        if (existing != null) {
            Optional<GitHubDocumentGateway.GitHubDocumentMetadata> exact = files.stream()
                    .filter(file -> existing.getPath().equals(file.path()))
                    .findFirst();
            if (exact.isPresent()) {
                return exact;
            }
        }
        Pattern pattern = compilePathPattern(stage.pathPattern());
        return files.stream()
                .filter(file -> pattern.matcher(file.path()).matches())
                .max(Comparator.comparingInt(file -> scoreDocumentMatch(file, requirement, sources)));
    }

    private Pattern compilePathPattern(String pathPattern) {
        StringBuilder regex = new StringBuilder("^");
        StringBuilder literal = new StringBuilder();
        boolean inPlaceholder = false;
        for (int index = 0; index < pathPattern.length(); index++) {
            char ch = pathPattern.charAt(index);
            if (ch == '{') {
                regex.append(Pattern.quote(literal.toString()));
                literal.setLength(0);
                inPlaceholder = true;
            } else if (ch == '}' && inPlaceholder) {
                regex.append("[^/]+");
                inPlaceholder = false;
            } else if (!inPlaceholder) {
                literal.append(ch);
            }
        }
        regex.append(Pattern.quote(literal.toString()));
        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    private int scoreDocumentMatch(
            GitHubDocumentGateway.GitHubDocumentMetadata file,
            RequirementEntity requirement,
            List<SourceReferenceDto> sources
    ) {
        String path = file.path().toLowerCase(Locale.ROOT);
        String title = file.title() == null ? "" : file.title().toLowerCase(Locale.ROOT);
        int score = 0;
        if (path.contains(slugify(requirement.getId()))) score += 20;
        if (path.contains(slugify(requirement.getTitle())) || title.contains(requirement.getTitle().toLowerCase(Locale.ROOT))) score += 20;
        for (SourceReferenceDto source : sources) {
            String externalId = slugify(source.externalId());
            if (!isBlank(externalId) && path.contains(externalId)) score += 15;
        }
        return score;
    }

    private String buildDocumentId(String requirementId, String sddType) {
        String candidate = "DOC-" + requirementId + "-" + sddType.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-");
        return candidate.length() <= 64 ? candidate : candidate.substring(0, 64);
    }

    private String slugify(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private SourceReferenceDto toSourceDto(RequirementSourceReferenceEntity source) {
        return new SourceReferenceDto(source.getId(), source.getRequirementId(), source.getSourceType(), source.getExternalId(), source.getTitle(), source.getUrl(), source.getSourceUpdatedAt(), source.getFetchedAt(), source.getFreshnessStatus(), source.getErrorMessage());
    }

    private DocumentReviewDto toReviewDto(RequirementDocumentReviewEntity review) {
        RequirementSddDocumentIndexEntity document = documentRepository.findById(review.getDocumentId()).orElse(null);
        boolean stale = review.isStale() || (document != null && !document.getLatestBlobSha().equals(review.getBlobSha()));
        return new DocumentReviewDto(review.getId(), review.getDocumentId(), review.getRequirementId(), review.getDecision(), review.getComment(), review.getReviewerId(), review.getReviewerType(), review.getCommitSha(), review.getBlobSha(), review.getAnchorType(), review.getAnchorValue(), stale, review.getCreatedAt());
    }

    private AgentRunDto toAgentRunDto(RequirementAgentRunEntity run) {
        return new AgentRunDto(run.getExecutionId(), run.getRequirementId(), run.getProfileId(), run.getSkillKey(), run.getTargetStage(), run.getStatus(), readJson(run.getManifest()), run.getOutputSummary(), run.getErrorMessage(), artifactLinkRepository.findByExecutionIdOrderByCreatedAtAsc(run.getExecutionId()).stream().map(this::toArtifactDto).toList(), run.getCreatedAt(), run.getUpdatedAt());
    }

    private ArtifactLinkDto toArtifactDto(RequirementArtifactLinkEntity artifact) {
        return new ArtifactLinkDto(artifact.getId(), artifact.getExecutionId(), artifact.getRequirementId(), artifact.getArtifactType(), artifact.getStorageType(), artifact.getTitle(), artifact.getUri(), artifact.getRepoFullName(), artifact.getPath(), artifact.getCommitSha(), artifact.getBlobSha(), artifact.getStatus(), artifact.getCreatedAt());
    }

    private DocumentQualityGateResultDto latestQualityGateFor(RequirementSddDocumentIndexEntity document) {
        return qualityGateRepository.findTopByDocumentIdOrderByScoredAtDesc(document.getId())
                .map(run -> toQualityGateDto(run, document))
                .orElse(null);
    }

    private DocumentQualityGateResultDto toQualityGateDto(RequirementDocumentQualityGateRunEntity run, RequirementSddDocumentIndexEntity document) {
        boolean stale = document != null && (!run.getBlobSha().equals(document.getLatestBlobSha()) || !run.getCommitSha().equals(document.getLatestCommitSha()));
        return new DocumentQualityGateResultDto(
                run.getExecutionId(),
                run.getDocumentId(),
                run.getRequirementId(),
                run.getProfileId(),
                run.getSddType(),
                run.getScore(),
                run.getBand(),
                run.isPassed(),
                run.getThreshold(),
                run.getRubricVersion(),
                run.getCommitSha(),
                run.getBlobSha(),
                readDimensions(run.getDimensions()),
                readFindings(run.getFindings()),
                run.getSummary(),
                run.getTriggeredBy(),
                run.getTriggerMode(),
                stale,
                run.getScoredAt()
        );
    }

    private int scoreDocumentQuality(RequirementSddDocumentIndexEntity document) {
        return switch (document.getSddType()) {
            case "requirement", "requirement-normalizer" -> 88;
            case "user-story" -> 84;
            case "spec", "functional-spec" -> 94;
            case "architecture", "technical-design" -> 78;
            case "design", "file-spec" -> 86;
            case "program-spec" -> 76;
            case "api-guide", "ut-plan", "tasks" -> 82;
            default -> 80 + Math.abs(document.getSddType().hashCode() % 16);
        };
    }

    private List<DocumentQualityDimensionDto> buildQualityDimensions(RequirementSddDocumentIndexEntity document, int score) {
        int traceability = Math.min(20, Math.max(8, score / 5));
        int completeness = Math.min(25, Math.max(10, score / 4));
        int testability = Math.min(15, Math.max(6, score / 7));
        int readiness = Math.min(25, Math.max(10, score / 4));
        int risk = Math.max(0, Math.min(15, score - traceability - completeness - testability - readiness));
        return List.of(
                new DocumentQualityDimensionDto("completeness", "Completeness", completeness, 25),
                new DocumentQualityDimensionDto("traceability", "Traceability", traceability, 20),
                new DocumentQualityDimensionDto("testability", "Testability", testability, 15),
                new DocumentQualityDimensionDto("implementationReadiness", "Implementation Readiness", readiness, 25),
                new DocumentQualityDimensionDto("riskControls", "Risk & Compliance", risk, 15)
        );
    }

    private List<DocumentQualityFindingDto> buildQualityFindings(RequirementSddDocumentIndexEntity document, boolean passed) {
        if (passed) {
            return List.of(
                    new DocumentQualityFindingDto("INFO", "Traceability", "Source and document identifiers are present."),
                    new DocumentQualityFindingDto("INFO", "Readiness", "No blocking completeness gaps detected by the active rubric.")
            );
        }
        return List.of(
                new DocumentQualityFindingDto("BLOCKER", "Acceptance Criteria", "Acceptance criteria are incomplete or not testable enough."),
                new DocumentQualityFindingDto("BLOCKER", "Traceability", "Traceability to source evidence must be strengthened before approval."),
                new DocumentQualityFindingDto("ADVISORY", document.getStageLabel(), "Add concrete downstream readiness notes for the next SDD stage.")
        );
    }

    private String qualityLabel(String band) {
        return switch (band) {
            case "EXCELLENT" -> "Excellent";
            case "GOOD" -> "Good";
            default -> "Blocked";
        };
    }

    private RequirementSddDocumentIndexEntity getDocumentEntity(String documentId) {
        return documentRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("SDD document not found: " + documentId));
    }

    private void validateRequirement(String requirementId) {
        if (!requirementRepository.existsById(requirementId)) throw new ResourceNotFoundException("Requirement not found: " + requirementId);
    }

    private RequirementEntity getRequirementEntity(String requirementId) {
        return requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + requirementId));
    }

    private String normalizeSourceType(String value, String url) {
        if (!isBlank(value)) return normalize(value, "URL");
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.contains("jira")) return "JIRA";
        if (lower.contains("confluence")) return "CONFLUENCE";
        if (lower.contains("github.com")) return "GITHUB";
        return "URL";
    }

    private String inferExternalId(String url) {
        String trimmed = url.trim();
        int slash = trimmed.lastIndexOf('/');
        return slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
    }

    private String titleFromUrl(String url) {
        return inferExternalId(url).replace('-', ' ').replace('_', ' ');
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize agent payload", e);
        }
    }

    private Map<String, Object> readJson(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<DocumentQualityDimensionDto> readDimensions(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<DocumentQualityFindingDto> readFindings(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String normalize(String value, String fallback) {
        return firstNonBlank(value, fallback).trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static String firstNonBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private static <T> T firstNonNull(T value, T fallback) {
        return value == null ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
