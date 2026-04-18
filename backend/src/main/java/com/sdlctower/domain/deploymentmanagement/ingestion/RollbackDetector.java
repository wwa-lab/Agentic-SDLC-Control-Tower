package com.sdlctower.domain.deploymentmanagement.ingestion;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.DeployEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.entity.ReleaseEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.DeployRepository;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.ReleaseRepository;
import com.sdlctower.domain.deploymentmanagement.policy.ReleaseVersionComparator;
import org.springframework.stereotype.Component;

@Component
public class RollbackDetector {

    private final DeployRepository deployRepo;
    private final ReleaseRepository releaseRepo;
    private final ReleaseVersionComparator comparator;

    public RollbackDetector(DeployRepository deployRepo, ReleaseRepository releaseRepo,
                             ReleaseVersionComparator comparator) {
        this.deployRepo = deployRepo;
        this.releaseRepo = releaseRepo;
        this.comparator = comparator;
    }

    public void detect(DeployEntity deploy, String releaseVersion) {
        if ("ROLLBACK".equals(deploy.getTrigger())) {
            deploy.setRollback(true);
            deploy.setRollbackDetectionSignal("trigger=rollback");
            return;
        }

        if (releaseVersion == null) return;

        var priorSuccess = deployRepo.findLastSuccessByApplicationAndEnvironment(
                deploy.getApplicationId(), deploy.getEnvironmentName());
        if (priorSuccess.isEmpty()) return;

        var priorReleaseId = priorSuccess.get().getReleaseId();
        if (priorReleaseId == null) return;

        var priorRelease = releaseRepo.findById(priorReleaseId);
        if (priorRelease.isEmpty()) return;

        if (comparator.compare(releaseVersion, priorRelease.get().getReleaseVersion()) < 0) {
            deploy.setRollback(true);
            deploy.setRollbackDetectionSignal("version-older-than-prior");
        }
    }
}
