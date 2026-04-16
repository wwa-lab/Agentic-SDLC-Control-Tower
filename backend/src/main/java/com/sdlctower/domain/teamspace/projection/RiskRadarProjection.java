package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.teamspace.TeamSpaceConstants;
import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.RiskActionDto;
import com.sdlctower.domain.teamspace.dto.RiskItemDto;
import com.sdlctower.domain.teamspace.dto.SkillAttributionDto;
import com.sdlctower.domain.teamspace.dto.TeamRiskRadarDto;
import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RiskRadarProjection implements TeamSpaceProjection<TeamRiskRadarDto> {

    private final TeamSpaceSeedCatalog seedCatalog;
    private final RiskSignalRepository riskSignalRepository;

    public RiskRadarProjection(TeamSpaceSeedCatalog seedCatalog, RiskSignalRepository riskSignalRepository) {
        this.seedCatalog = seedCatalog;
        this.riskSignalRepository = riskSignalRepository;
    }

    @Override
    public TeamRiskRadarDto load(String workspaceId) {
        seedCatalog.workspace(workspaceId);

        List<RiskSignalEntity> openSignals = riskSignalRepository.findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc(workspaceId);
        Map<String, List<RiskItemDto>> groups = new LinkedHashMap<>();
        groups.put("INCIDENT", openSignals.stream().filter(signal -> "INCIDENT".equals(signal.getCategory())).map(this::toDto).toList());
        groups.put("APPROVAL", openSignals.stream().filter(signal -> "APPROVAL".equals(signal.getCategory())).map(this::toDto).toList());
        groups.put("CONFIG_DRIFT", openSignals.stream().filter(signal -> "CONFIG_DRIFT".equals(signal.getCategory())).map(this::toDto).toList());
        groups.put("DEPENDENCY", openSignals.stream().filter(signal -> "DEPENDENCY".equals(signal.getCategory())).map(this::toDto).toList());
        groups.put("PROJECT", openSignals.stream().filter(signal -> "PROJECT".equals(signal.getCategory())).map(this::toDto).toList());

        Instant lastRefreshed = openSignals.stream()
                .map(RiskSignalEntity::getDetectedAt)
                .max(Comparator.naturalOrder())
                .orElse(TeamSpaceConstants.REFERENCE_NOW);

        return new TeamRiskRadarDto(groups, lastRefreshed, openSignals.size());
    }

    private RiskItemDto toDto(RiskSignalEntity signal) {
        return new RiskItemDto(
                signal.getId(),
                signal.getCategory(),
                signal.getSeverity(),
                signal.getTitle(),
                signal.getDetail(),
                (int) ChronoUnit.DAYS.between(signal.getDetectedAt(), TeamSpaceConstants.REFERENCE_NOW),
                new RiskActionDto(signal.getActionLabel(), signal.getActionUrl()),
                signal.getSkillName() == null ? null : new SkillAttributionDto(signal.getSkillName(), signal.getExecutionId())
        );
    }
}
