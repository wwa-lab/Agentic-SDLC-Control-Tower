package com.sdlctower.domain.deploymentmanagement.events;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeploymentChangeLogEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.DeploymentChangeLogRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class DeploymentChangeLogPublisher {

    private final DeploymentChangeLogRepository repo;

    public DeploymentChangeLogPublisher(DeploymentChangeLogRepository repo) {
        this.repo = repo;
    }

    public void publish(String entityType, String entityId, String entryType,
                        String actor, String detail) {
        repo.save(DeploymentChangeLogEntity.create(
                UUID.randomUUID().toString(), entityType, entityId, entryType, actor, detail));
    }
}
