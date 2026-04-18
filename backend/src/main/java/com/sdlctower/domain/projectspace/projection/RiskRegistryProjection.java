package com.sdlctower.domain.projectspace.projection;

import com.sdlctower.domain.projectspace.ProjectSpaceConstants;
import com.sdlctower.domain.projectspace.ProjectSpaceProjection;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.RiskItemDto;
import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.RiskRegistryDto;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import com.sdlctower.domain.teamspace.dto.SkillAttributionDto;
import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RiskRegistryProjection implements ProjectSpaceProjection<RiskRegistryDto> {

    private static final Map<String, Integer> SEVERITY_RANK = Map.of(
            "CRITICAL", 0,
            "HIGH", 1,
            "MEDIUM", 2,
            "LOW", 3
    );

    private final ProjectSpaceSeedCatalog seedCatalog;
    private final RiskSignalRepository riskSignalRepository;

    public RiskRegistryProjection(ProjectSpaceSeedCatalog seedCatalog, RiskSignalRepository riskSignalRepository) {
        this.seedCatalog = seedCatalog;
        this.riskSignalRepository = riskSignalRepository;
    }

    @Override
    public RiskRegistryDto load(String projectId) {
        seedCatalog.project(projectId);
        List<RiskSignalEntity> signals = riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(projectId).stream()
                .sorted(Comparator
                        .comparingInt((RiskSignalEntity signal) -> SEVERITY_RANK.getOrDefault(signal.getSeverity().toUpperCase(), 99))
                        .thenComparing(RiskSignalEntity::getDetectedAt, Comparator.reverseOrder()))
                .toList();

        Instant lastRefreshed = signals.stream()
                .map(RiskSignalEntity::getDetectedAt)
                .max(Comparator.naturalOrder())
                .orElse(ProjectSpaceConstants.REFERENCE_NOW);

        return new RiskRegistryDto(
                signals.stream().map(signal -> toDto(projectId, signal)).toList(),
                signals.size(),
                lastRefreshed
        );
    }

    private RiskItemDto toDto(String projectId, RiskSignalEntity signal) {
        return new RiskItemDto(
                signal.getId(),
                signal.getTitle(),
                signal.getSeverity(),
                signal.getCategory(),
                seedCatalog.riskOwner(projectId, signal.getCategory()),
                (int) ChronoUnit.DAYS.between(signal.getDetectedAt(), ProjectSpaceConstants.REFERENCE_NOW),
                signal.getDetail(),
                new LinkDto(signal.getActionUrl(), true),
                signal.getSkillName() == null ? null : new SkillAttributionDto(signal.getSkillName(), signal.getExecutionId())
        );
    }
}
