package com.sdlctower.domain.deploymentmanagement.policy;

import org.springframework.stereotype.Component;
import java.util.Set;

@Component("deploymentAiAutonomyPolicy")
public class AiAutonomyPolicy {

    private static final Set<String> ALLOWED = Set.of("SUPERVISED", "AUTONOMOUS");

    public void assertCanRegenerate(String autonomyLevel) {
        if (!ALLOWED.contains(autonomyLevel)) {
            throw new DeploymentException("DP_AI_AUTONOMY_INSUFFICIENT",
                    "AI autonomy level must be SUPERVISED or higher; got " + autonomyLevel);
        }
    }
}
