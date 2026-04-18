package com.sdlctower.domain.designmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignArtifactRepository extends JpaRepository<DesignArtifactEntity, String> {

    List<DesignArtifactEntity> findByWorkspaceIdOrderByUpdatedAtDesc(String workspaceId);
}
