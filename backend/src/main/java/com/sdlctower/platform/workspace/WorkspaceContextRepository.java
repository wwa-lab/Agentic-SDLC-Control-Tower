package com.sdlctower.platform.workspace;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceContextRepository extends JpaRepository<WorkspaceContext, Long> {

    Optional<WorkspaceContext> findTopByOrderByIdAsc();
}
