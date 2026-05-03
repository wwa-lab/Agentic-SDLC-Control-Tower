package com.sdlctower.platform.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditRepository extends JpaRepository<AuditRecordEntity, String> {

    @Query("""
            select a from AuditRecordEntity a
            where (:category is null or a.category = :category)
              and (:actor is null or a.actor = :actor)
              and (:objectType is null or a.objectType = :objectType)
              and (:objectId is null or a.objectId = :objectId)
              and (:outcome is null or a.outcome = :outcome)
              and (:scopeType is null or a.scopeType = :scopeType)
              and (:scopeId is null or a.scopeId = :scopeId)
              and (:fromTime is null or a.eventTime >= :fromTime)
            order by a.eventTime desc, a.id desc
            """)
    List<AuditRecordEntity> search(
            @Param("category") String category,
            @Param("actor") String actor,
            @Param("objectType") String objectType,
            @Param("objectId") String objectId,
            @Param("outcome") String outcome,
            @Param("scopeType") String scopeType,
            @Param("scopeId") String scopeId,
            @Param("fromTime") Instant fromTime
    );
}
