package com.sdlctower.platform.template;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlatformTemplateRepository extends JpaRepository<PlatformTemplateEntity, String> {

    @Query("""
            select t from PlatformTemplateEntity t
            where (:kind is null or t.kind = :kind)
              and (:status is null or t.status = :status)
              and (
                :q is null
                or lower(t.key) like lower(concat('%', :q, '%'))
                or lower(t.name) like lower(concat('%', :q, '%'))
              )
            order by t.updatedAt desc, t.id asc
            """)
    List<PlatformTemplateEntity> search(
            @Param("kind") String kind,
            @Param("status") String status,
            @Param("q") String q
    );
}
