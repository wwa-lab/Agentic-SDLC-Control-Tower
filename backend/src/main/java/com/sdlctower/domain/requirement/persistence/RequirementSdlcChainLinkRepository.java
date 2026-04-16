package com.sdlctower.domain.requirement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementSdlcChainLinkRepository extends JpaRepository<RequirementSdlcChainLinkEntity, Long> {

    List<RequirementSdlcChainLinkEntity> findByRequirementIdOrderByLinkOrderAsc(String requirementId);
}
