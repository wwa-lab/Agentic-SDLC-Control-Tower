package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.RiskDto;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("pmRiskRegistryProjection")
public class RiskRegistryProjection {

    private final RiskSignalRepository riskSignalRepository;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final ProjectManagementMapper mapper;

    public RiskRegistryProjection(
            RiskSignalRepository riskSignalRepository,
            RevisionFencingPolicy revisionFencingPolicy,
            ProjectManagementMapper mapper
    ) {
        this.riskSignalRepository = riskSignalRepository;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.mapper = mapper;
    }

    public List<RiskDto> load(String projectId, String state, String severity) {
        long revision = revisionFencingPolicy.currentRevision(projectId);
        return riskSignalRepository.findByProjectIdOrderByDetectedAtDesc(projectId).stream()
                .filter(risk -> state == null || state.equalsIgnoreCase(mapper.riskState(risk)))
                .filter(risk -> severity == null || severity.equalsIgnoreCase(risk.getSeverity()))
                .map(risk -> mapper.toRiskDto(risk, revision))
                .sorted(Comparator
                        .comparingInt((RiskDto risk) -> ProjectManagementProjectionSupport.severityRank(risk.severity())).reversed()
                        .thenComparing(RiskDto::ageDays, Comparator.reverseOrder()))
                .toList();
    }
}
