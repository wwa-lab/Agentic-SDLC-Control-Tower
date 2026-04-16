package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.MemberMatrixDto;
import org.springframework.stereotype.Component;

@Component
public class MemberProjection implements TeamSpaceProjection<MemberMatrixDto> {

    private final TeamSpaceSeedCatalog seedCatalog;

    public MemberProjection(TeamSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public MemberMatrixDto load(String workspaceId) {
        return seedCatalog.members(workspaceId);
    }
}
