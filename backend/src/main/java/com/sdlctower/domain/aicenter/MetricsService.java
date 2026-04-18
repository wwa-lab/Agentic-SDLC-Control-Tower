package com.sdlctower.domain.aicenter;

import com.sdlctower.domain.aicenter.dto.MetricValueDto;
import com.sdlctower.domain.aicenter.dto.MetricsSummaryDto;
import com.sdlctower.domain.aicenter.dto.StageCoverageDto;
import com.sdlctower.domain.aicenter.dto.StageCoverageEntryDto;
import com.sdlctower.domain.aicenter.persistence.SkillExecutionRepository;
import com.sdlctower.domain.aicenter.persistence.SkillRepository;
import com.sdlctower.domain.aicenter.persistence.SkillStageRepository;
import com.sdlctower.shared.dto.SectionResultDto;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private static final List<String[]> CANONICAL_STAGES = List.of(
            new String[]{"requirement",  "Requirement"},
            new String[]{"user-story",   "User Story"},
            new String[]{"spec",         "Spec"},
            new String[]{"architecture", "Architecture"},
            new String[]{"design",       "Design"},
            new String[]{"tasks",        "Tasks"},
            new String[]{"code",         "Code"},
            new String[]{"test",         "Test"},
            new String[]{"deploy",       "Deploy"},
            new String[]{"incident",     "Incident"},
            new String[]{"learning",     "Learning"}
    );

    private final SkillRepository skillRepository;
    private final SkillStageRepository skillStageRepository;
    private final SkillExecutionRepository executionRepository;

    public MetricsService(SkillRepository skillRepository,
                          SkillStageRepository skillStageRepository,
                          SkillExecutionRepository executionRepository) {
        this.skillRepository = skillRepository;
        this.skillStageRepository = skillStageRepository;
        this.executionRepository = executionRepository;
    }

    public MetricsSummaryDto summarize(String workspaceId, String window) {
        Duration duration = parseDuration(window);
        Instant since = Instant.now().minus(duration);

        return new MetricsSummaryDto(
                window,
                loadSection("aiUsageRate", () -> buildAiUsageRate(workspaceId, since)),
                loadSection("adoptionRate", () -> buildAdoptionRate(workspaceId)),
                loadSection("autoExecSuccessRate", () -> buildAutoExecSuccessRate(workspaceId, since)),
                loadSection("timeSavedHours", () -> buildTimeSavedHours(workspaceId, since)),
                loadSection("stageCoverageCount", () -> buildStageCoverageCount(workspaceId))
        );
    }

    public StageCoverageDto stageCoverage(String workspaceId) {
        List<Object[]> counts = skillStageRepository.countActiveSkillsByStage(workspaceId);
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Object[] row : counts) {
            countMap.put((String) row[0], (Long) row[1]);
        }

        List<StageCoverageEntryDto> entries = CANONICAL_STAGES.stream()
                .map(stage -> {
                    String key = stage[0];
                    String label = stage[1];
                    long count = countMap.getOrDefault(key, 0L);
                    return new StageCoverageEntryDto(key, label, (int) count, count > 0);
                })
                .toList();

        return new StageCoverageDto(entries);
    }

    private MetricValueDto buildAiUsageRate(String workspaceId, Instant since) {
        long total = executionRepository.countAllSince(workspaceId, since);
        double value = total > 0 ? 100.0 * total / Math.max(total, 1) : 0;
        return new MetricValueDto(Math.round(value * 10.0) / 10.0, "%", 0, "flat", true);
    }

    private MetricValueDto buildAdoptionRate(String workspaceId) {
        long activeSkills = skillRepository.countByWorkspaceIdAndStatus(workspaceId, "active");
        long totalSkills = skillRepository.findByWorkspaceIdOrderByNameAsc(workspaceId).size();
        double rate = totalSkills > 0 ? 100.0 * activeSkills / totalSkills : 0;
        return new MetricValueDto(Math.round(rate * 10.0) / 10.0, "%", 0, "flat", true);
    }

    private MetricValueDto buildAutoExecSuccessRate(String workspaceId, Instant since) {
        long succeeded = executionRepository.countSucceededSince(workspaceId, since);
        long total = executionRepository.countAllSince(workspaceId, since);
        double rate = total > 0 ? 100.0 * succeeded / total : 0;
        return new MetricValueDto(Math.round(rate * 10.0) / 10.0, "%", 0, "flat", true);
    }

    private MetricValueDto buildTimeSavedHours(String workspaceId, Instant since) {
        long minutes = executionRepository.sumTimeSavedMinutesSince(workspaceId, since);
        double hours = minutes / 60.0;
        return new MetricValueDto(Math.round(hours * 10.0) / 10.0, "hours", 0, "flat", true);
    }

    private MetricValueDto buildStageCoverageCount(String workspaceId) {
        List<Object[]> counts = skillStageRepository.countActiveSkillsByStage(workspaceId);
        long covered = counts.stream().filter(r -> ((Long) r[1]) > 0).count();
        return new MetricValueDto(covered, "count", 0, "flat", true);
    }

    private Duration parseDuration(String window) {
        return switch (window) {
            case "24h" -> Duration.ofHours(24);
            case "7d"  -> Duration.ofDays(7);
            default    -> Duration.ofDays(30);
        };
    }

    private <T> SectionResultDto<T> loadSection(String name, Supplier<T> builder) {
        try {
            return SectionResultDto.ok(builder.get());
        } catch (Exception e) {
            log.error("Failed to load AI Center metric [{}]: {}", name, e.getMessage(), e);
            return SectionResultDto.fail("Failed to load " + name);
        }
    }
}
