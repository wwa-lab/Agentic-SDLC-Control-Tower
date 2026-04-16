package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.TeamOperatingModelDto;
import org.springframework.stereotype.Component;

@Component
public class OperatingModelProjection implements TeamSpaceProjection<TeamOperatingModelDto> {

    private final TeamSpaceSeedCatalog seedCatalog;

    public OperatingModelProjection(TeamSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public TeamOperatingModelDto load(String workspaceId) {
        return seedCatalog.operatingModel(workspaceId);
    }
}
