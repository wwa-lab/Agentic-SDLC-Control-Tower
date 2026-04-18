package com.sdlctower.shared.integration.requirement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SpecRevisionLookup {

    Map<String, SpecRevisionInfo> findByIds(Collection<String> specIds);

    List<SpecRevisionInfo> listByWorkspace(String workspaceId);

    record SpecRevisionInfo(
            String specId,
            String title,
            int latestRevision,
            String state,
            String requirementId,
            String projectId,
            String workspaceId
    ) {}
}
