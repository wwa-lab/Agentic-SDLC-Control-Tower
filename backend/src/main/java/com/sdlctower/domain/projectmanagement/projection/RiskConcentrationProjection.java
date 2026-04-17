package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioRiskConcentrationDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.RiskDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.SeverityCategoryCountDto;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RiskConcentrationProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final RiskSignalRepository riskSignalRepository;
    private final ProjectManagementMapper mapper;

    public RiskConcentrationProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            RiskSignalRepository riskSignalRepository,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.riskSignalRepository = riskSignalRepository;
        this.mapper = mapper;
    }

    public PortfolioRiskConcentrationDto load(String workspaceId, Integer limit, String severity, String category) {
        List<RiskDto> risks = projectSeedCatalog.projectsForWorkspace(workspaceId).stream()
                .flatMap(project -> riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc(project.id()).stream())
                .filter(risk -> severity == null || severity.equalsIgnoreCase(risk.getSeverity()))
                .filter(risk -> category == null || category.equalsIgnoreCase(risk.getCategory()))
                .map(risk -> mapper.toRiskDto(risk, risk.getPlanRevisionAtUpdate()))
                .sorted(Comparator
                        .comparingInt((RiskDto risk) -> ProjectManagementProjectionSupport.severityRank(risk.severity())).reversed()
                        .thenComparing(RiskDto::ageDays, Comparator.reverseOrder()))
                .limit(limit == null ? 20 : limit)
                .toList();

        List<SeverityCategoryCountDto> heatmap = risks.stream()
                .collect(Collectors.groupingBy(
                        risk -> risk.severity() + "|" + risk.category(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    return new SeverityCategoryCountDto(parts[0], parts[1], entry.getValue());
                })
                .toList();

        return new PortfolioRiskConcentrationDto(risks, heatmap);
    }
}
