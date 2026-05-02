package com.sdlctower.platform.workspace;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceKeyAliasRepository extends JpaRepository<WorkspaceKeyAliasEntity, Long> {

    Optional<WorkspaceKeyAliasEntity> findByFormerKeyAndExpiresAtAfter(String formerKey, Instant now);
}
