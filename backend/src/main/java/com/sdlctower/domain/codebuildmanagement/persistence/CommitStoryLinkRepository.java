package com.sdlctower.domain.codebuildmanagement.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitStoryLinkRepository extends JpaRepository<CommitStoryLinkEntity, String> {

    List<CommitStoryLinkEntity> findByStoryIdIn(Collection<String> storyIds);

    List<CommitStoryLinkEntity> findByLinkStatus(String linkStatus, Pageable pageable);

    List<CommitStoryLinkEntity> findByCommitId(String commitId);
}
