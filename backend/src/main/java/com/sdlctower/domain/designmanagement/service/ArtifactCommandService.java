package com.sdlctower.domain.designmanagement.service;

import com.sdlctower.domain.designmanagement.DesignManagementConstants;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.PublishVersionRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RegisterArtifactRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RetireArtifactRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.UpdateMetadataRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MutationResultDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ArtifactFormat;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ArtifactLifecycle;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ChangeLogEntryType;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactAuthorEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactAuthorRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogRepository;
import com.sdlctower.domain.designmanagement.policy.DesignAccessGuard;
import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.domain.designmanagement.policy.LifecyclePolicy;
import com.sdlctower.domain.designmanagement.policy.VersionFencingPolicy;
import com.sdlctower.domain.designmanagement.security.HtmlSanitizer;
import com.sdlctower.domain.designmanagement.security.PiiScanner;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArtifactCommandService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignArtifactVersionRepository versionRepository;
    private final DesignArtifactAuthorRepository authorRepository;
    private final DesignChangeLogRepository changeLogRepository;
    private final DesignAccessGuard accessGuard;
    private final VersionFencingPolicy versionFencingPolicy;
    private final LifecyclePolicy lifecyclePolicy;
    private final PiiScanner piiScanner;
    private final HtmlSanitizer htmlSanitizer;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final AiSummaryService aiSummaryService;

    public ArtifactCommandService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactVersionRepository versionRepository,
            DesignArtifactAuthorRepository authorRepository,
            DesignChangeLogRepository changeLogRepository,
            DesignAccessGuard accessGuard,
            VersionFencingPolicy versionFencingPolicy,
            LifecyclePolicy lifecyclePolicy,
            PiiScanner piiScanner,
            HtmlSanitizer htmlSanitizer,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            AiSummaryService aiSummaryService
    ) {
        this.artifactRepository = artifactRepository;
        this.versionRepository = versionRepository;
        this.authorRepository = authorRepository;
        this.changeLogRepository = changeLogRepository;
        this.accessGuard = accessGuard;
        this.versionFencingPolicy = versionFencingPolicy;
        this.lifecyclePolicy = lifecyclePolicy;
        this.piiScanner = piiScanner;
        this.htmlSanitizer = htmlSanitizer;
        this.projectSeedCatalog = projectSeedCatalog;
        this.aiSummaryService = aiSummaryService;
    }

    public MutationResultDto register(RegisterArtifactRequest request) {
        projectSeedCatalog.project(request.projectId());
        accessGuard.requireAdmin(request.projectId());
        ArtifactLifecycle lifecycle = DesignManagementEnums.parse(ArtifactLifecycle.class, request.lifecycle(), "lifecycle");
        ArtifactFormat format = DesignManagementEnums.parse(ArtifactFormat.class, request.format(), "format");
        String sanitizedHtml = validateAndSanitize(request.htmlPayload());
        Instant now = Instant.now();
        String actorId = accessGuard.currentActor().memberId();
        String artifactId = "art-" + UUID.randomUUID().toString().substring(0, 8);
        String versionId = artifactId + "-v1";

        DesignArtifactEntity artifact = DesignArtifactEntity.create(
                artifactId,
                projectSeedCatalog.project(request.projectId()).workspaceId(),
                request.projectId(),
                request.title().trim(),
                format.name(),
                lifecycle.name(),
                versionId,
                actorId,
                now,
                now
        );
        DesignArtifactVersionEntity version = DesignArtifactVersionEntity.create(
                versionId,
                artifactId,
                1,
                sanitizedHtml,
                DesignManagementConstants.sizeBytes(sanitizedHtml),
                sha256(sanitizedHtml),
                request.changeLogNote(),
                actorId,
                now
        );
        artifactRepository.save(artifact);
        versionRepository.save(version);
        replaceAuthors(artifactId, request.authorIds(), actorId);
        appendLog(artifactId, ChangeLogEntryType.REGISTERED.name(), actorId, null, "{\"title\":\"" + artifact.getTitle() + "\"}", "Artifact registered");
        appendLog(artifactId, ChangeLogEntryType.VERSION_PUBLISHED.name(), actorId, null, "{\"versionId\":\"" + versionId + "\"}", "Initial version published");
        aiSummaryService.generateForVersion(artifact, version);

        return new MutationResultDto(artifactId, versionId, 1, artifact.getLifecycle(), "Artifact registered.");
    }

    public MutationResultDto publishVersion(String artifactId, PublishVersionRequest request) {
        versionFencingPolicy.check(artifactId, request.prevVersionId());
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        String sanitizedHtml = validateAndSanitize(request.htmlPayload());
        int nextVersionNumber = versionRepository.findByArtifactIdOrderByVersionNumberDesc(artifactId).stream()
                .findFirst()
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);
        String versionId = artifactId + "-v" + nextVersionNumber;
        Instant now = Instant.now();
        String actorId = accessGuard.currentActor().memberId();

        DesignArtifactVersionEntity version = DesignArtifactVersionEntity.create(
                versionId,
                artifactId,
                nextVersionNumber,
                sanitizedHtml,
                DesignManagementConstants.sizeBytes(sanitizedHtml),
                sha256(sanitizedHtml),
                request.changeLogNote(),
                actorId,
                now
        );
        versionRepository.save(version);
        artifact.setCurrentVersionId(versionId);
        artifact.setUpdatedAt(now);
        artifactRepository.save(artifact);
        appendLog(artifactId, ChangeLogEntryType.VERSION_PUBLISHED.name(), actorId, "{\"prevVersionId\":\"" + request.prevVersionId() + "\"}", "{\"versionId\":\"" + versionId + "\"}", "Version published");
        aiSummaryService.generateForVersion(artifact, version);

        return new MutationResultDto(artifactId, versionId, nextVersionNumber, artifact.getLifecycle(), "Version published.");
    }

    public MutationResultDto retire(String artifactId, RetireArtifactRequest request) {
        versionFencingPolicy.check(artifactId, request.prevVersionId());
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        String previousLifecycle = artifact.getLifecycle();
        lifecyclePolicy.requireCanTransition(ArtifactLifecycle.valueOf(previousLifecycle), ArtifactLifecycle.RETIRED);
        artifact.setLifecycle(ArtifactLifecycle.RETIRED.name());
        artifact.setUpdatedAt(Instant.now());
        artifactRepository.save(artifact);
        String actorId = accessGuard.currentActor().memberId();
        appendLog(artifactId, ChangeLogEntryType.RETIRED.name(), actorId, null, "{\"lifecycle\":\"RETIRED\"}", request.reason());
        appendLog(artifactId, ChangeLogEntryType.LIFECYCLE_TRANSITIONED.name(), actorId, "{\"lifecycle\":\"" + previousLifecycle + "\"}", "{\"lifecycle\":\"RETIRED\"}", request.reason());
        return new MutationResultDto(artifactId, artifact.getCurrentVersionId(), currentVersionNumber(artifactId), artifact.getLifecycle(), "Artifact retired.");
    }

    public MutationResultDto updateMetadata(String artifactId, UpdateMetadataRequest request) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        if (request.title() != null && !request.title().isBlank()) {
            artifact.setTitle(request.title().trim());
        }
        if (request.format() != null && !request.format().isBlank()) {
            artifact.setFormat(DesignManagementEnums.parse(ArtifactFormat.class, request.format(), "format").name());
        }
        artifact.setUpdatedAt(Instant.now());
        artifactRepository.save(artifact);
        if (request.authorIds() != null) {
            replaceAuthors(artifactId, request.authorIds(), accessGuard.currentActor().memberId());
        }
        appendLog(artifactId, ChangeLogEntryType.METADATA_UPDATED.name(), accessGuard.currentActor().memberId(), null, "{\"title\":\"" + artifact.getTitle() + "\"}", "Artifact metadata updated");
        return new MutationResultDto(artifactId, artifact.getCurrentVersionId(), currentVersionNumber(artifactId), artifact.getLifecycle(), "Metadata updated.");
    }

    private String validateAndSanitize(String htmlPayload) {
        if (DesignManagementConstants.sizeBytes(htmlPayload) > DesignManagementConstants.HTML_SIZE_LIMIT_BYTES) {
            throw DesignManagementException.invalid("DM_ARTIFACT_TOO_LARGE", "HTML payload exceeds 2 MiB limit");
        }
        List<PiiScanner.PiiMatch> piiMatches = piiScanner.scan(htmlPayload);
        if (!piiMatches.isEmpty()) {
            PiiScanner.PiiMatch match = piiMatches.getFirst();
            throw DesignManagementException.invalid("DM_PII_DETECTED", "Detected " + match.kind() + " sample: " + match.sample());
        }
        return htmlSanitizer.sanitize(htmlPayload);
    }

    private void replaceAuthors(String artifactId, List<String> authorIds, String fallbackActorId) {
        authorRepository.deleteByIdArtifactId(artifactId);
        List<String> resolvedAuthorIds = (authorIds == null || authorIds.isEmpty()) ? List.of(fallbackActorId) : authorIds.stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
        authorRepository.saveAll(resolvedAuthorIds.stream()
                .map(authorId -> DesignArtifactAuthorEntity.create(artifactId, authorId))
                .toList());
    }

    private void appendLog(String artifactId, String entryType, String actorId, String beforeJson, String afterJson, String reason) {
        changeLogRepository.save(DesignChangeLogEntity.create(
                "dlog-" + UUID.randomUUID().toString().substring(0, 8),
                artifactId,
                entryType,
                actorId,
                null,
                beforeJson,
                afterJson,
                reason,
                "corr-" + UUID.randomUUID().toString().substring(0, 8),
                Instant.now()
        ));
    }

    private int currentVersionNumber(String artifactId) {
        return versionRepository.findByArtifactIdOrderByVersionNumberDesc(artifactId).stream()
                .findFirst()
                .map(DesignArtifactVersionEntity::getVersionNumber)
                .orElse(0);
    }

    private String sha256(String html) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(html.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash HTML payload", ex);
        }
    }
}
