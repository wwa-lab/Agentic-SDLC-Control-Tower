package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyBottleneckDto;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionRepository;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DependencyBottlenecksProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final ProjectDependencyRepository dependencyRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final ProjectManagementMapper mapper;

    public DependencyBottlenecksProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            ProjectDependencyRepository dependencyRepository,
            AiSuggestionRepository aiSuggestionRepository,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.dependencyRepository = dependencyRepository;
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.mapper = mapper;
    }

    public List<DependencyBottleneckDto> load(String workspaceId, Integer limit) {
        return projectSeedCatalog.projectsForWorkspace(workspaceId).stream()
                .flatMap(project -> dependencyRepository.findBySourceProjectIdOrderByCreatedAtAsc(project.id()).stream())
                .filter(dependency -> dependency.getBlockerReason() != null && !"RESOLVED".equals(mapper.dependencyState(dependency)))
                .map(dependency -> new DependencyBottleneckDto(
                        dependency.getId(),
                        dependency.getSourceProjectId(),
                        projectSeedCatalog.project(dependency.getSourceProjectId()).name(),
                        dependency.getTargetProjectId(),
                        dependency.getTargetName(),
                        dependency.isExternal(),
                        dependency.getRelationship(),
                        dependency.getBlockerReason(),
                        dependency.getOwnerTeam(),
                        Math.max(0, java.time.Duration.between(dependency.getCreatedAt(), com.sdlctower.domain.projectmanagement.ProjectManagementConstants.REFERENCE_NOW).toDays()),
                        aiSuggestionRepository.findByProjectIdAndStateOrderByCreatedAtDesc(dependency.getSourceProjectId(), "PENDING").stream()
                                .filter(suggestion -> suggestion.getTargetId().equals(dependency.getId()))
                                .map(com.sdlctower.domain.projectmanagement.persistence.AiSuggestionEntity::getId)
                                .findFirst()
                                .orElse(null)
                ))
                .sorted(Comparator.comparingLong(DependencyBottleneckDto::daysBlocked).reversed())
                .limit(limit == null ? 15 : limit)
                .toList();
    }
}
