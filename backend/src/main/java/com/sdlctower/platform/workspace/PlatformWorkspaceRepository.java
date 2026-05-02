package com.sdlctower.platform.workspace;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlatformWorkspaceRepository extends JpaRepository<PlatformWorkspaceEntity, String> {

    Optional<PlatformWorkspaceEntity> findByWorkspaceKey(String workspaceKey);

    List<PlatformWorkspaceEntity> findByApplicationId(String applicationId);

    List<PlatformWorkspaceEntity> findBySnowGroupId(String snowGroupId);

    @Query("""
        SELECT pw FROM PlatformWorkspaceEntity pw
        WHERE pw.id IN (
            SELECT ra.scopeId FROM RoleAssignmentEntity ra
            WHERE ra.staffId = :staffId
              AND ra.scopeType = 'workspace'
              AND ra.role IN ('WORKSPACE_ADMIN', 'WORKSPACE_MEMBER', 'WORKSPACE_VIEWER')
        )
        OR EXISTS (
            SELECT 1 FROM RoleAssignmentEntity ra
            WHERE ra.staffId = :staffId
              AND ra.scopeType = 'platform'
              AND ra.scopeId = '*'
        )
        """)
    List<PlatformWorkspaceEntity> findAccessibleByStaffId(String staffId);
}
