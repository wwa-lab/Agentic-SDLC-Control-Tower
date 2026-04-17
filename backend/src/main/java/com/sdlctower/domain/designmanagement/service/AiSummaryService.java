package com.sdlctower.domain.designmanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.designmanagement.DesignManagementConstants;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.RegenerateAiSummaryRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MutationResultDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.AiSummaryStatus;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ChangeLogEntryType;
import com.sdlctower.domain.designmanagement.integration.AiSkillClient;
import com.sdlctower.domain.designmanagement.integration.AiSkillClient.AiSummaryResult;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignAiSummaryRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactVersionRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkRepository;
import com.sdlctower.domain.designmanagement.policy.AiAutonomyPolicy;
import com.sdlctower.domain.designmanagement.policy.DesignAccessGuard;
import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import com.sdlctower.shared.integration.workspace.WorkspaceAutonomyLookup.WorkspaceAutonomyLevel;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignArtifactVersionRepository versionRepository;
    private final DesignSpecLinkRepository linkRepository;
    private final DesignAiSummaryRepository summaryRepository;
    private final DesignChangeLogRepository changeLogRepository;
    private final DesignAccessGuard accessGuard;
    private final AiAutonomyPolicy aiAutonomyPolicy;
    private final SpecRevisionLookup specRevisionLookup;
    private final AiSkillClient aiSkillClient;
    private final ObjectMapper objectMapper;

    public AiSummaryService(
            DesignArtifactRepository artifactRepository,
            DesignArtifactVersionRepository versionRepository,
            DesignSpecLinkRepository linkRepository,
            DesignAiSummaryRepository summaryRepository,
            DesignChangeLogRepository changeLogRepository,
            DesignAccessGuard accessGuard,
            AiAutonomyPolicy aiAutonomyPolicy,
            SpecRevisionLookup specRevisionLookup,
            AiSkillClient aiSkillClient,
            ObjectMapper objectMapper
    ) {
        this.artifactRepository = artifactRepository;
        this.versionRepository = versionRepository;
        this.linkRepository = linkRepository;
        this.summaryRepository = summaryRepository;
        this.changeLogRepository = changeLogRepository;
        this.accessGuard = accessGuard;
        this.aiAutonomyPolicy = aiAutonomyPolicy;
        this.specRevisionLookup = specRevisionLookup;
        this.aiSkillClient = aiSkillClient;
        this.objectMapper = objectMapper;
    }

    public MutationResultDto regenerate(String artifactId, RegenerateAiSummaryRequest request) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        aiAutonomyPolicy.requireAtLeast(artifact.getWorkspaceId(), WorkspaceAutonomyLevel.SUPERVISED);
        DesignArtifactVersionEntity version = versionRepository.findById(artifact.getCurrentVersionId())
                .orElseThrow(() -> DesignManagementException.notFound("DM_VERSION_NOT_FOUND", "Version not found: " + artifact.getCurrentVersionId()));
        DesignAiSummaryEntity summary = generateSummary(artifact, version, request.skillVersion());
        changeLogRepository.save(DesignChangeLogEntity.create(
                "dlog-" + UUID.randomUUID().toString().substring(0, 8),
                artifact.getId(),
                ChangeLogEntryType.AI_SUMMARY_REGENERATED.name(),
                accessGuard.currentActor().memberId(),
                null,
                null,
                "{\"summaryId\":\"" + summary.getId() + "\"}",
                "AI summary regenerated",
                "corr-" + UUID.randomUUID().toString().substring(0, 8),
                Instant.now()
        ));
        return new MutationResultDto(artifact.getId(), version.getId(), version.getVersionNumber(), artifact.getLifecycle(), "AI summary regenerated.");
    }

    public void generateForVersion(DesignArtifactEntity artifact, DesignArtifactVersionEntity version) {
        generateSummary(artifact, version, DesignManagementConstants.DEFAULT_SKILL_VERSION);
    }

    private DesignAiSummaryEntity generateSummary(DesignArtifactEntity artifact, DesignArtifactVersionEntity version, String requestedSkillVersion) {
        String skillVersion = requestedSkillVersion == null || requestedSkillVersion.isBlank()
                ? DesignManagementConstants.DEFAULT_SKILL_VERSION
                : requestedSkillVersion;
        List<String> specIds = linkRepository.findByArtifactId(artifact.getId()).stream().map(link -> link.getSpecId()).toList();
        List<SpecRevisionInfo> linkedSpecs = specRevisionLookup.findByIds(specIds).values().stream().toList();
        Instant now = Instant.now();
        try {
            AiSummaryResult result = aiSkillClient.summarize(artifact.getTitle(), version.getHtmlPayload(), linkedSpecs, skillVersion);
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "summaryText", result.summaryText(),
                    "keyElements", result.keyElements()
            ));
            DesignAiSummaryEntity entity = DesignAiSummaryEntity.create(
                    "dais-" + UUID.randomUUID().toString().substring(0, 8),
                    artifact.getId(),
                    version.getId(),
                    result.skillVersion(),
                    AiSummaryStatus.SUCCESS.name(),
                    payloadJson,
                    null,
                    now
            );
            return summaryRepository.save(entity);
        } catch (Exception ex) {
            return summaryRepository.save(DesignAiSummaryEntity.create(
                    "dais-" + UUID.randomUUID().toString().substring(0, 8),
                    artifact.getId(),
                    version.getId(),
                    skillVersion,
                    AiSummaryStatus.FAILED.name(),
                    null,
                    encodeError(ex),
                    now
            ));
        }
    }

    private String encodeError(Exception ex) {
        try {
            return objectMapper.writeValueAsString(Map.of("message", ex.getMessage()));
        } catch (JsonProcessingException nested) {
            return "{\"message\":\"" + ex.getMessage() + "\"}";
        }
    }
}
