package com.sdlctower.domain.testingmanagement.integration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface RequirementLookup {

    Optional<RequirementRef> find(String reqId);

    Map<String, RequirementRef> findByIds(Collection<String> reqIds);

    record RequirementRef(
            String storyId,
            String reqId,
            String title,
            String projectId,
            String state
    ) {}
}
