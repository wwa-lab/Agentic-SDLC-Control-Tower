package com.sdlctower.domain.designmanagement.service;

import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.LinkSpecsRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementCommands.UnlinkSpecRequest;
import com.sdlctower.domain.designmanagement.dto.DesignManagementDtos.MutationResultDto;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ChangeLogEntryType;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.CoverageDeclaration;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignArtifactRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignChangeLogRepository;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkEntity;
import com.sdlctower.domain.designmanagement.persistence.DesignSpecLinkRepository;
import com.sdlctower.domain.designmanagement.policy.DesignAccessGuard;
import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SpecLinkCommandService {

    private final DesignArtifactRepository artifactRepository;
    private final DesignSpecLinkRepository linkRepository;
    private final DesignChangeLogRepository changeLogRepository;
    private final DesignAccessGuard accessGuard;
    private final SpecRevisionLookup specRevisionLookup;

    public SpecLinkCommandService(
            DesignArtifactRepository artifactRepository,
            DesignSpecLinkRepository linkRepository,
            DesignChangeLogRepository changeLogRepository,
            DesignAccessGuard accessGuard,
            SpecRevisionLookup specRevisionLookup
    ) {
        this.artifactRepository = artifactRepository;
        this.linkRepository = linkRepository;
        this.changeLogRepository = changeLogRepository;
        this.accessGuard = accessGuard;
        this.specRevisionLookup = specRevisionLookup;
    }

    public MutationResultDto linkSpecs(String artifactId, LinkSpecsRequest request) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        Map<String, SpecRevisionLookup.SpecRevisionInfo> specs = specRevisionLookup.findByIds(request.links().stream().map(link -> link.specId()).toList());
        String actorId = accessGuard.currentActor().memberId();
        for (var linkRequest : request.links()) {
            if (!specs.containsKey(linkRequest.specId())) {
                throw DesignManagementException.badRequest("DM_SPEC_NOT_FOUND", "Unknown spec: " + linkRequest.specId());
            }
            CoverageDeclaration declaration = DesignManagementEnums.parse(CoverageDeclaration.class, linkRequest.declaredCoverage(), "declaredCoverage");
            DesignSpecLinkEntity entity = linkRepository.findByArtifactIdAndSpecId(artifactId, linkRequest.specId())
                    .orElseGet(() -> DesignSpecLinkEntity.create(
                            "dsl-" + UUID.randomUUID().toString().substring(0, 8),
                            artifactId,
                            linkRequest.specId(),
                            linkRequest.coversRevision(),
                            declaration.name(),
                            actorId,
                            Instant.now()
                    ));
            entity.setDeclaredCoverage(declaration.name());
            entity.setCoversRevision(linkRequest.coversRevision());
            entity.setLinkedByMemberId(actorId);
            entity.setLinkedAt(Instant.now());
            linkRepository.save(entity);
            changeLogRepository.save(DesignChangeLogEntity.create(
                    "dlog-" + UUID.randomUUID().toString().substring(0, 8),
                    artifactId,
                    ChangeLogEntryType.SPEC_LINKED.name(),
                    actorId,
                    null,
                    null,
                    "{\"specId\":\"" + linkRequest.specId() + "\"}",
                    "Spec linked",
                    "corr-" + UUID.randomUUID().toString().substring(0, 8),
                    Instant.now()
            ));
        }
        return new MutationResultDto(artifactId, artifact.getCurrentVersionId(), null, artifact.getLifecycle(), "Specs linked.");
    }

    public MutationResultDto unlinkSpec(String artifactId, String specId, UnlinkSpecRequest request) {
        DesignArtifactEntity artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_ARTIFACT_NOT_FOUND", "Artifact not found: " + artifactId));
        accessGuard.requireAdmin(artifact.getProjectId());
        DesignSpecLinkEntity link = linkRepository.findByArtifactIdAndSpecId(artifactId, specId)
                .orElseThrow(() -> DesignManagementException.notFound("DM_SPEC_LINK_NOT_FOUND", "Spec link not found for " + specId));
        linkRepository.delete(link);
        changeLogRepository.save(DesignChangeLogEntity.create(
                "dlog-" + UUID.randomUUID().toString().substring(0, 8),
                artifactId,
                ChangeLogEntryType.SPEC_UNLINKED.name(),
                accessGuard.currentActor().memberId(),
                null,
                "{\"specId\":\"" + specId + "\"}",
                null,
                request.reason(),
                "corr-" + UUID.randomUUID().toString().substring(0, 8),
                Instant.now()
        ));
        return new MutationResultDto(artifactId, artifact.getCurrentVersionId(), null, artifact.getLifecycle(), "Spec unlinked.");
    }
}
