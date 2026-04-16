package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.ProjectDistributionDto;
import org.springframework.stereotype.Component;

@Component
public class ProjectDistributionProjection implements TeamSpaceProjection<ProjectDistributionDto> {

    private final TeamSpaceSeedCatalog seedCatalog;

    public ProjectDistributionProjection(TeamSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public ProjectDistributionDto load(String workspaceId) {
        return seedCatalog.projects(workspaceId);
    }
}
