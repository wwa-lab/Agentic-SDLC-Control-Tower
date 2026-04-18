package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.MilestoneDto;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MilestonePlanProjection {

    private final MilestoneRepository milestoneRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final ProjectManagementMapper mapper;

    public MilestonePlanProjection(
            MilestoneRepository milestoneRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            ProjectManagementMapper mapper
    ) {
        this.milestoneRepository = milestoneRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.mapper = mapper;
    }

    public List<MilestoneDto> load(String projectId, boolean includeArchived) {
        long revision = revisionFencingPolicy.currentRevision(projectId);
        return milestoneRepository.findByProjectIdOrderByTargetDateAsc(projectId).stream()
                .filter(milestone -> includeArchived || !"ARCHIVED".equals(mapper.milestoneState(milestone)))
                .map(milestone -> mapper.toMilestoneDto(milestone, revision))
                .toList();
    }
}
