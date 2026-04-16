package com.sdlctower.domain.requirement.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementRepository extends JpaRepository<RequirementEntity, String> {}
