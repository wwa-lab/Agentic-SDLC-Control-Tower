package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckRunRepository extends JpaRepository<CheckRunEntity, String> {

    List<CheckRunEntity> findByRepoIdAndHeadSha(String repoId, String headSha);
}
