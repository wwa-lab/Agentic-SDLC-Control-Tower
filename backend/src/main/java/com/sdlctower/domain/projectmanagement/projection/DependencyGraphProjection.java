package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyDto;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DependencyGraphProjection {

    private final ProjectDependencyRepository dependencyRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final ProjectManagementMapper mapper;

    public DependencyGraphProjection(
            ProjectDependencyRepository dependencyRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            ProjectManagementMapper mapper
    ) {
        this.dependencyRepository = dependencyRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.mapper = mapper;
    }

    public List<DependencyDto> load(String projectId, String state) {
        long revision = revisionFencingPolicy.currentRevision(projectId);
        return dependencyRepository.findBySourceProjectIdOrderByCreatedAtAsc(projectId).stream()
                .filter(dependency -> state == null || state.equalsIgnoreCase(mapper.dependencyState(dependency)))
                .map(dependency -> mapper.toDependencyDto(dependency, revision))
                .toList();
    }
}
