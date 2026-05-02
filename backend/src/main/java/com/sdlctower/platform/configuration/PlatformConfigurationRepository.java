package com.sdlctower.platform.configuration;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlatformConfigurationRepository extends JpaRepository<PlatformConfigurationEntity, String> {

    @Query("""
            select c from PlatformConfigurationEntity c
            where (:kind is null or c.kind = :kind)
              and (:scopeType is null or c.scopeType = :scopeType)
              and (:scopeId is null or c.scopeId = :scopeId)
              and (:status is null or c.status = :status)
              and (:q is null or lower(c.key) like lower(concat('%', :q, '%')))
            order by c.lastModifiedAt desc, c.id asc
            """)
    List<PlatformConfigurationEntity> search(
            @Param("kind") String kind,
            @Param("scopeType") String scopeType,
            @Param("scopeId") String scopeId,
            @Param("status") String status,
            @Param("q") String q
    );

    Optional<PlatformConfigurationEntity> findByKeyAndScopeTypeAndScopeId(String key, String scopeType, String scopeId);

    Optional<PlatformConfigurationEntity> findByKeyAndScopeTypeAndScopeIdAndStatus(String key, String scopeType, String scopeId, String status);
}
