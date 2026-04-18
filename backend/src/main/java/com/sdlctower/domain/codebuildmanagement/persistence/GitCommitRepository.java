package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitCommitRepository extends JpaRepository<GitCommitEntity, String> {

    List<GitCommitEntity> findTop20ByRepoIdAndBranchNameOrderByCommittedAtDesc(String repoId, String branchName);

    Optional<GitCommitEntity> findByRepoIdAndSha(String repoId, String sha);

    List<GitCommitEntity> findByIdIn(Collection<String> ids);
}
