package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunRerunRatePolicy")
public class RunRerunRatePolicy {

    private static final Duration WINDOW = Duration.ofHours(1);
    private static final int LIMIT = CodeBuildManagementConstants.RERUN_RATE_LIMIT_PER_HOUR;

    private final Map<String, List<Instant>> windows = new ConcurrentHashMap<>();

    public void check(String repoId) {
        Instant cutoff = Instant.now().minus(WINDOW);
        List<Instant> entries = windows.getOrDefault(repoId, List.of());

        long recentCount = entries.stream()
                .filter(ts -> ts.isAfter(cutoff))
                .count();

        if (recentCount >= LIMIT) {
            throw CodeBuildManagementException.tooManyRequests(
                    "CB_GH_RATE_LIMIT",
                    "Rerun rate limit exceeded for repo " + repoId
                            + ": max " + LIMIT + " reruns per hour");
        }
    }

    public void record(String repoId) {
        windows.compute(repoId, (key, existing) -> {
            Instant cutoff = Instant.now().minus(WINDOW);
            List<Instant> cleaned = new ArrayList<>();
            if (existing != null) {
                for (Instant ts : existing) {
                    if (ts.isAfter(cutoff)) {
                        cleaned.add(ts);
                    }
                }
            }
            cleaned.add(Instant.now());
            return cleaned;
        });
    }
}
