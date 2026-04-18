package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MilestoneDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MilestoneHubDto;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MilestoneProjection implements ProjectSpaceProjection<MilestoneHubDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final MilestoneRepository milestoneRepository;

    public MilestoneProjection(ProjectSpaceSeedCatalog seedCatalog, MilestoneRepository milestoneRepository) {
        this.seedCatalog = seedCatalog;
        this.milestoneRepository = milestoneRepository;
    }

    @Override
    public MilestoneHubDto load(String projectId) {
        List<MilestoneDto> milestones = milestoneRepository.findByProjectIdOrderByOrderingAsc(projectId).stream()
                .map(milestone -> new MilestoneDto(
                        milestone.getId(),
                        milestone.getLabel(),
                        milestone.getTargetDate(),
                        milestone.getStatus(),
                        milestone.getPercentComplete(),
                        seedCatalog.nullableMemberRef(milestone.getOwnerMemberId()),
                        milestone.isCurrent(),
                        milestone.getSlippageReason()
                ))
                .toList();

        return new MilestoneHubDto(
                milestones,
                new LinkDto("/project-management?projectId=" + projectId, false)
        );
    }
}
