package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.DependencyDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.DependencyMapDto;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyEntity;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DependencyProjection implements ProjectSpaceProjection<DependencyMapDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final ProjectDependencyRepository projectDependencyRepository;

    public DependencyProjection(
            ProjectSpaceSeedCatalog seedCatalog,
            ProjectDependencyRepository projectDependencyRepository
    ) {
        this.seedCatalog = seedCatalog;
        this.projectDependencyRepository = projectDependencyRepository;
    }

    @Override
    public DependencyMapDto load(String projectId) {
        ProjectSpaceSeedCatalog.ProjectSeed project = seedCatalog.project(projectId);
        return new DependencyMapDto(
                projectDependencyRepository.findBySourceProjectIdAndDirectionOrderByCreatedAtAsc(projectId, "UPSTREAM").stream()
                        .map(entity -> toDto(project, entity))
                        .toList(),
                projectDependencyRepository.findBySourceProjectIdAndDirectionOrderByCreatedAtAsc(projectId, "DOWNSTREAM").stream()
                        .map(entity -> toDto(project, entity))
                        .toList()
        );
    }

    private DependencyDto toDto(ProjectSpaceSeedCatalog.ProjectSeed project, ProjectDependencyEntity entity) {
        LinkDto primaryAction = null;
        if (entity.getTargetProjectId() != null) {
            primaryAction = new LinkDto("/project-space/" + entity.getTargetProjectId() + "?workspaceId=" + project.workspaceId(), true);
        } else if (entity.getBlockerReason() != null) {
            primaryAction = new LinkDto("/incidents/INC-0422", true);
        }

        return new DependencyDto(
                entity.getId(),
                entity.getTargetName(),
                entity.getTargetRef(),
                entity.getTargetProjectId(),
                entity.isExternal(),
                entity.getDirection(),
                entity.getRelationship(),
                entity.getOwnerTeam(),
                entity.getHealth(),
                entity.getBlockerReason(),
                primaryAction
        );
    }
}
