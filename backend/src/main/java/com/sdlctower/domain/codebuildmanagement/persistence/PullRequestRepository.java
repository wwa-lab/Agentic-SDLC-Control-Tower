package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String> {

    List<PullRequestEntity> findTop15ByRepoIdOrderByUpdatedAtDesc(String repoId);

    Optional<PullRequestEntity> findByRepoIdAndPrNumber(String repoId, int prNumber);

    List<PullRequestEntity> findByHeadSha(String headSha);
}
