package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.LeadershipOwnershipDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.RoleAssignmentDto;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LeadershipProjection implements ProjectSpaceProjection<LeadershipOwnershipDto> {

    private final ProjectSpaceSeedCatalog seedCatalog;

    public LeadershipProjection(ProjectSpaceSeedCatalog seedCatalog) {
        this.seedCatalog = seedCatalog;
    }

    @Override
    public LeadershipOwnershipDto load(String projectId) {
        List<RoleAssignmentDto> assignments = seedCatalog.project(projectId).roles().stream()
                .map(role -> new RoleAssignmentDto(
                        role.role(),
                        role.memberId(),
                        role.memberId() == null ? null : seedCatalog.memberDisplayName(role.memberId()),
                        role.oncallStatus(),
                        role.backupMemberId() != null,
                        role.backupMemberId(),
                        role.backupMemberId() == null ? null : seedCatalog.memberDisplayName(role.backupMemberId())
                ))
                .toList();

        return new LeadershipOwnershipDto(
                assignments,
                new LinkDto("/platform?view=access&projectId=" + projectId, false)
        );
    }
}
