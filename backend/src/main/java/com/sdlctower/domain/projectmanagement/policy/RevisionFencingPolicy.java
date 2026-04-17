package com.sdlctower.domain.projectmanagement.policy;

import com.sdlctower.domain.projectmanagement.persistence.ProjectPlanRevisionEntity;
import com.sdlctower.domain.projectmanagement.persistence.ProjectPlanRevisionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevisionFencingPolicy {

    private final ProjectPlanRevisionRepository revisionRepository;

    public RevisionFencingPolicy(ProjectPlanRevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    public long currentRevision(String projectId) {
        return revisionRepository.findById(projectId)
                .map(ProjectPlanRevisionEntity::getCurrentRevision)
                .orElseGet(() -> initialize(projectId));
    }

    public void check(String projectId, Long clientRevision) {
        if (clientRevision == null) {
            throw ProjectManagementException.invalid(
                    "PM_STALE_REVISION",
                    "planRevision is required for project " + projectId
            );
        }
        long currentRevision = currentRevision(projectId);
        if (clientRevision != currentRevision) {
            throw ProjectManagementException.conflict(
                    "PM_STALE_REVISION",
                    "Current revision is " + currentRevision + ", provided " + clientRevision
            );
        }
    }

    @Transactional
    public long bump(String projectId) {
        ProjectPlanRevisionEntity entity = revisionRepository.findById(projectId)
                .orElseGet(() -> ProjectPlanRevisionEntity.create(projectId, 0));
        entity.setCurrentRevision(entity.getCurrentRevision() + 1);
        revisionRepository.save(entity);
        return entity.getCurrentRevision();
    }

    @Transactional
    protected long initialize(String projectId) {
        return revisionRepository.save(ProjectPlanRevisionEntity.create(projectId, 0)).getCurrentRevision();
    }
}
