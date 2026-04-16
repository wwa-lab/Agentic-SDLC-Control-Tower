package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.TeamDefaultTemplatesDto;
import org.springframework.stereotype.Component;

@Component
public class TemplateInheritanceProjection implements TeamSpaceProjection<TeamDefaultTemplatesDto> {

    private final TeamSpaceSeedCatalog seedCatalog;

    public TemplateInheritanceProjection(TeamSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public TeamDefaultTemplatesDto load(String workspaceId) {
        return seedCatalog.templates(workspaceId);
    }
}
