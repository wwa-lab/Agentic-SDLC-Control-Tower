package com.sdlctower.platform.policy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlatformPolicyRepository extends JpaRepository<PlatformPolicyEntity, String> {

    @Query("""
            select p from PlatformPolicyEntity p
            where (:category is null or p.category = :category)
              and (:status is null or p.status = :status)
              and (:scopeType is null or p.scopeType = :scopeType)
              and (:scopeId is null or p.scopeId = :scopeId)
              and (:boundTo is null or p.boundTo = :boundTo)
              and (:q is null or lower(p.key) like lower(concat('%', :q, '%')) or lower(p.name) like lower(concat('%', :q, '%')))
            order by p.createdAt desc, p.id desc
            """)
    List<PlatformPolicyEntity> search(
            @Param("category") String category,
            @Param("status") String status,
            @Param("scopeType") String scopeType,
            @Param("scopeId") String scopeId,
            @Param("boundTo") String boundTo,
            @Param("q") String q
    );

    Optional<PlatformPolicyEntity> findByKeyAndScopeTypeAndScopeIdAndStatus(String key, String scopeType, String scopeId, String status);
}
