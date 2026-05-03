package com.sdlctower.platform.access;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlatformUserRepository extends JpaRepository<PlatformUserEntity, String> {

    @Query("""
            select u from PlatformUserEntity u
            where (:status is null or lower(u.status) = lower(:status))
              and (:profileSource is null or lower(u.profileSource) = lower(:profileSource))
              and (
                :q is null
                or lower(u.staffId) like lower(concat('%', :q, '%'))
                or lower(u.displayName) like lower(concat('%', :q, '%'))
                or lower(coalesce(u.staffName, '')) like lower(concat('%', :q, '%'))
                or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))
              )
            order by u.staffId asc
            """)
    List<PlatformUserEntity> search(
            @Param("status") String status,
            @Param("profileSource") String profileSource,
            @Param("q") String q
    );
}
