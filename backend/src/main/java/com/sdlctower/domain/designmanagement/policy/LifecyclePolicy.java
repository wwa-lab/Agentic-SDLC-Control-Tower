package com.sdlctower.domain.designmanagement.policy;

import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.ArtifactLifecycle;
import org.springframework.stereotype.Component;

@Component
public class LifecyclePolicy {

    public void requireCanTransition(ArtifactLifecycle from, ArtifactLifecycle to) {
        boolean allowed = switch (from) {
            case DRAFT -> to == ArtifactLifecycle.PUBLISHED || to == ArtifactLifecycle.RETIRED;
            case PUBLISHED -> to == ArtifactLifecycle.RETIRED;
            case RETIRED -> false;
        };
        if (!allowed) {
            throw DesignManagementException.invalid(
                    "DM_INVALID_LIFECYCLE_TRANSITION",
                    "Cannot transition artifact lifecycle from " + from + " to " + to
            );
        }
    }
}
