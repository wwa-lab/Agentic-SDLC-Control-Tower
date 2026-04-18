package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.RegenerateDeploySummaryResponse;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.AiRowStatus;
import com.sdlctower.domain.deploymentmanagement.persistence.entity.AiDeploymentSummaryEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.AiDeploymentSummaryRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class AiDeploymentSummaryService {

    private final AiDeploymentSummaryRepository repo;

    public AiDeploymentSummaryService(AiDeploymentSummaryRepository repo) {
        this.repo = repo;
    }

    public RegenerateDeploySummaryResponse enqueueRegeneration(String deployId) {
        var entity = AiDeploymentSummaryEntity.createPending(
                UUID.randomUUID().toString(), deployId, "deploy-summary-v2");
        repo.save(entity);
        return new RegenerateDeploySummaryResponse(deployId, AiRowStatus.PENDING, Instant.now());
    }
}
