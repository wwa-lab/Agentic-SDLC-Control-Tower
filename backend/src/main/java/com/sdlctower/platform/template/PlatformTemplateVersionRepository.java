package com.sdlctower.platform.template;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformTemplateVersionRepository extends JpaRepository<PlatformTemplateVersionEntity, String> {

    List<PlatformTemplateVersionEntity> findByTemplateIdOrderByVersionNumberDesc(String templateId);

    Optional<PlatformTemplateVersionEntity> findFirstByTemplateIdOrderByVersionNumberDesc(String templateId);
}
