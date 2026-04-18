package com.sdlctower.domain.designmanagement.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignSpecLinkRepository extends JpaRepository<DesignSpecLinkEntity, String> {

    List<DesignSpecLinkEntity> findByArtifactId(String artifactId);

    List<DesignSpecLinkEntity> findByArtifactIdIn(Collection<String> artifactIds);

    Optional<DesignSpecLinkEntity> findByArtifactIdAndSpecId(String artifactId, String specId);

    void deleteByArtifactIdAndSpecId(String artifactId, String specId);
}
