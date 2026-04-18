package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.RegenerateReleaseNotesResponse;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.AiRowStatus;
import com.sdlctower.domain.deploymentmanagement.persistence.entity.AiReleaseNotesEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.AiReleaseNotesRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class AiReleaseNotesService {

    private final AiReleaseNotesRepository repo;

    public AiReleaseNotesService(AiReleaseNotesRepository repo) {
        this.repo = repo;
    }

    public RegenerateReleaseNotesResponse enqueueRegeneration(String releaseId) {
        var entity = AiReleaseNotesEntity.createPending(
                UUID.randomUUID().toString(), releaseId, "release-notes-v3");
        repo.save(entity);
        return new RegenerateReleaseNotesResponse(releaseId, AiRowStatus.PENDING, Instant.now());
    }
}
