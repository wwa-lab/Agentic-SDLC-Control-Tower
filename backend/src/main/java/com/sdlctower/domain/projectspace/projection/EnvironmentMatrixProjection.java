package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceConstants;
import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.EnvironmentDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.EnvironmentMatrixDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.VersionDriftDto;
import com.sdlctower.domain.projectspace.persistence.DeploymentEntity;
import com.sdlctower.domain.projectspace.persistence.DeploymentRepository;
import com.sdlctower.domain.projectspace.persistence.EnvironmentEntity;
import com.sdlctower.domain.projectspace.persistence.EnvironmentRepository;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentMatrixProjection implements ProjectSpaceProjection<EnvironmentMatrixDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final EnvironmentRepository environmentRepository;
    private final DeploymentRepository deploymentRepository;

    public EnvironmentMatrixProjection(
            ProjectSpaceSeedCatalog seedCatalog,
            EnvironmentRepository environmentRepository,
            DeploymentRepository deploymentRepository
    ) {
        this.seedCatalog = seedCatalog;
        this.environmentRepository = environmentRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @Override
    public EnvironmentMatrixDto load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = seedCatalog.project(projectId);
        if (project.environmentProjectionFails()) {
            throw new IllegalStateException("Environment feed unavailable for " + projectId);
        }

        List<EnvironmentEntity> environments = environmentRepository.findByProjectIdOrderByLabelAsc(projectId);
        String prodVersion = environments.stream()
                .filter(environment -> "PROD".equalsIgnoreCase(environment.getKind()))
                .findFirst()
                .flatMap(environment -> deploymentRepository.findFirstByEnvironmentIdOrderByDeployedAtDesc(environment.getId()))
                .map(DeploymentEntity::getVersionRef)
                .orElse("unknown");

        return new EnvironmentMatrixDto(
                environments.stream()
                        .map(environment -> toDto(projectId, prodVersion, environment))
                        .toList()
        );
    }

    public static String resolveDriftBand(int commitDelta) {
        if (commitDelta <= 0) {
            return "NONE";
        }
        if (commitDelta <= ProjectSpaceConstants.VERSION_DRIFT_MINOR_MAX) {
            return "MINOR";
        }
        return "MAJOR";
    }

    private EnvironmentDto toDto(String projectId, String prodVersion, EnvironmentEntity environment) {
        Optional<DeploymentEntity> latestDeployment = deploymentRepository.findFirstByEnvironmentIdOrderByDeployedAtDesc(environment.getId());
        DeploymentEntity deployment = latestDeployment.orElse(null);

        VersionDriftDto drift = null;
        if (deployment != null && !"PROD".equalsIgnoreCase(environment.getKind()) && deployment.getCommitDistanceFromProd() > 0) {
            drift = new VersionDriftDto(
                    resolveDriftBand(deployment.getCommitDistanceFromProd()),
                    deployment.getCommitDistanceFromProd(),
                    prodVersion,
                    environment.getLabel() + " trails PROD by " + deployment.getCommitDistanceFromProd() + " commits"
            );
        }

        return new EnvironmentDto(
                environment.getId(),
                environment.getLabel(),
                environment.getKind(),
                deployment == null ? "UNDEPLOYED" : deployment.getVersionRef(),
                deployment == null ? "N/A" : deployment.getBuildId(),
                deployment == null ? "UNKNOWN" : deployment.getHealth(),
                environment.getGateStatus(),
                seedCatalog.nullableMemberRef(environment.getApproverMemberId()),
                deployment == null ? ProjectSpaceConstants.REFERENCE_NOW : deployment.getDeployedAt(),
                drift,
                new LinkDto("/deployment?projectId=" + projectId + "&envId=" + environment.getId(), false)
        );
    }
}
