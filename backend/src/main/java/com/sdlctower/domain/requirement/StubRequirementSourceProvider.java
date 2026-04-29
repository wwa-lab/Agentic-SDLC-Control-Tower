package com.sdlctower.domain.requirement;

import com.sdlctower.domain.requirement.persistence.RequirementSourceReferenceEntity;
import java.time.Instant;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class StubRequirementSourceProvider implements RequirementSourceProvider {
    @Override
    public boolean supports(String sourceType) {
        return true;
    }

    @Override
    public SourceMetadata refresh(RequirementSourceReferenceEntity source) {
        Instant now = Instant.now();
        return new SourceMetadata(
                source.getExternalId(),
                source.getTitle(),
                now,
                now,
                "FRESH",
                null
        );
    }
}
