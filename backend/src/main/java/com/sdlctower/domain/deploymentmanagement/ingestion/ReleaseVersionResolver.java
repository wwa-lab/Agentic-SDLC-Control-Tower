package com.sdlctower.domain.deploymentmanagement.ingestion;

import com.sdlctower.domain.deploymentmanagement.persistence.entity.ReleaseEntity;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.ReleaseRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class ReleaseVersionResolver {

    private final ReleaseRepository releaseRepo;

    public ReleaseVersionResolver(ReleaseRepository releaseRepo) {
        this.releaseRepo = releaseRepo;
    }

    public String resolveOrCreate(String applicationId, String releaseVersion) {
        if (releaseVersion == null || releaseVersion.isBlank()) {
            return null;
        }
        return releaseRepo.findByApplicationIdAndReleaseVersion(applicationId, releaseVersion)
                .map(ReleaseEntity::getId)
                .orElseGet(() -> {
                    var entity = ReleaseEntity.create(
                            "release-" + UUID.randomUUID().toString().substring(0, 8),
                            applicationId, releaseVersion, "PREPARED");
                    releaseRepo.save(entity);
                    return entity.getId();
                });
    }
}
