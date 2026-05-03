package com.sdlctower.platform.access;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, String> {

    @Query("""
            select r from RoleAssignmentEntity r
            where (:staffId is null or r.staffId = :staffId)
              and (:role is null or r.role = :role)
              and (:scopeType is null or r.scopeType = :scopeType)
              and (:scopeId is null or r.scopeId = :scopeId)
            order by r.grantedAt desc
            """)
    List<RoleAssignmentEntity> search(
            @Param("staffId") String staffId,
            @Param("role") String role,
            @Param("scopeType") String scopeType,
            @Param("scopeId") String scopeId
    );

    Optional<RoleAssignmentEntity> findByStaffIdAndRoleAndScopeTypeAndScopeId(
            String staffId,
            String role,
            String scopeType,
            String scopeId
    );

    @Query("""
            select count(r) from RoleAssignmentEntity r
            join PlatformUserEntity u on u.staffId = r.staffId
            where r.role = 'PLATFORM_ADMIN'
              and r.scopeType = 'platform'
              and r.scopeId = '*'
              and lower(u.status) = 'active'
            """)
    long countActivePlatformAdmins();
}
