package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<BranchEntity, String> {

    List<BranchEntity> findByRepoIdOrderByNameAsc(String repoId);

    Optional<BranchEntity> findByRepoIdAndName(String repoId, String name);
}
