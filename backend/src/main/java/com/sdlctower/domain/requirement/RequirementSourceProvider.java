package com.sdlctower.domain.requirement;

import com.sdlctower.domain.requirement.persistence.RequirementSourceReferenceEntity;
import java.time.Instant;

public interface RequirementSourceProvider {
    boolean supports(String sourceType);

    SourceMetadata refresh(RequirementSourceReferenceEntity source);

    record SourceMetadata(
            String externalId,
            String title,
            Instant sourceUpdatedAt,
            Instant fetchedAt,
            String freshnessStatus,
            String errorMessage
    ) {}
}
