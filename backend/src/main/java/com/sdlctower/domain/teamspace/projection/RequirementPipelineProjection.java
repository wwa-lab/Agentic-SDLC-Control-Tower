package com.sdlctower.domain.teamspace.projection;

import com.sdlctower.domain.requirement.persistence.RequirementEntity;
import com.sdlctower.domain.requirement.persistence.RequirementRepository;
import com.sdlctower.domain.requirement.persistence.RequirementSpecEntity;
import com.sdlctower.domain.requirement.persistence.RequirementSpecRepository;
import com.sdlctower.domain.requirement.persistence.UserStoryEntity;
import com.sdlctower.domain.requirement.persistence.UserStoryRepository;
import com.sdlctower.domain.teamspace.TeamSpaceConstants;
import com.sdlctower.domain.teamspace.TeamSpaceProjection;
import com.sdlctower.domain.teamspace.TeamSpaceSeedCatalog;
import com.sdlctower.domain.teamspace.dto.ChainNodeHealthDto;
import com.sdlctower.domain.teamspace.dto.PipelineBlockerDto;
import com.sdlctower.domain.teamspace.dto.PipelineCountersDto;
import com.sdlctower.domain.teamspace.dto.RequirementPipelineDto;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RequirementPipelineProjection implements TeamSpaceProjection<RequirementPipelineDto> {

    private static final int BLOCKER_THRESHOLD_DAYS = 3;

    private final TeamSpaceSeedCatalog seedCatalog;
    private final RequirementRepository requirementRepository;
    private final UserStoryRepository userStoryRepository;
    private final RequirementSpecRepository requirementSpecRepository;
    private final RiskSignalRepository riskSignalRepository;

    public RequirementPipelineProjection(
            TeamSpaceSeedCatalog seedCatalog,
            RequirementRepository requirementRepository,
            UserStoryRepository userStoryRepository,
            RequirementSpecRepository requirementSpecRepository,
            RiskSignalRepository riskSignalRepository
    ) {
        this.seedCatalog = seedCatalog;
        this.requirementRepository = requirementRepository;
        this.userStoryRepository = userStoryRepository;
        this.requirementSpecRepository = requirementSpecRepository;
        this.riskSignalRepository = riskSignalRepository;
    }

    @Override
    public RequirementPipelineDto load(String workspaceId) {
        TeamSpaceSeedCatalog.WorkspaceSeed workspace = seedCatalog.workspace(workspaceId);

        List<RequirementEntity> requirements = requirementRepository.findAll().stream()
                .filter(requirement -> workspace.requirementWorkspaceId().equals(requirement.getWorkspaceId()))
                .toList();
        Map<String, RequirementEntity> requirementById = requirements.stream()
                .collect(Collectors.toMap(RequirementEntity::getId, Function.identity()));

        List<UserStoryEntity> stories = userStoryRepository.findAll().stream()
                .filter(story -> requirementById.containsKey(story.getRequirementId()))
                .toList();

        List<RequirementSpecEntity> specs = requirementSpecRepository.findAll().stream()
                .filter(spec -> requirementById.containsKey(spec.getRequirementId()))
                .toList();

        List<RequirementSpecEntity> blockedSpecs = specs.stream()
                .filter(spec -> "Draft".equalsIgnoreCase(spec.getStatus()) || "Review".equalsIgnoreCase(spec.getStatus()))
                .filter(spec -> ageDays(requirementById.get(spec.getRequirementId()).getCreatedAt()) > BLOCKER_THRESHOLD_DAYS)
                .sorted(Comparator.comparing(spec -> requirementById.get(spec.getRequirementId()).getCreatedAt()))
                .toList();

        PipelineCountersDto counters = new PipelineCountersDto(
                (int) requirements.stream()
                        .filter(requirement -> requirement.getCreatedAt().isAfter(TeamSpaceConstants.REFERENCE_NOW.minus(7, ChronoUnit.DAYS)))
                        .count(),
                (int) stories.stream()
                        .filter(story -> !"Done".equalsIgnoreCase(story.getStatus()))
                        .count(),
                (int) specs.stream().filter(spec -> "Draft".equalsIgnoreCase(spec.getStatus())).count(),
                (int) specs.stream().filter(spec -> "Review".equalsIgnoreCase(spec.getStatus())).count(),
                blockedSpecs.size(),
                (int) specs.stream()
                        .filter(spec -> "Approved".equalsIgnoreCase(spec.getStatus()))
                        .filter(spec -> {
                            RequirementEntity requirement = requirementById.get(spec.getRequirementId());
                            return requirement != null
                                    && !"Delivered".equalsIgnoreCase(requirement.getStatus())
                                    && !"Archived".equalsIgnoreCase(requirement.getStatus());
                        })
                        .count()
        );

        List<PipelineBlockerDto> blockers = blockedSpecs.stream()
                .limit(5)
                .map(spec -> new PipelineBlockerDto(
                        "SPEC_BLOCKED",
                        spec.getId(),
                        spec.getTitle(),
                        ageDays(requirementById.get(spec.getRequirementId()).getCreatedAt()),
                        "/requirements?filter=blocked-specs&workspaceId=" + workspaceId
                ))
                .toList();

        boolean hasCriticalIncident = riskSignalRepository.findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc(workspaceId).stream()
                .anyMatch(risk -> "CRITICAL".equalsIgnoreCase(risk.getSeverity()));

        return new RequirementPipelineDto(
                counters,
                blockers,
                List.of(
                        new ChainNodeHealthDto("REQUIREMENT", "GREEN", false),
                        new ChainNodeHealthDto("USER_STORY", counters.storiesDecomposing() > 20 ? "YELLOW" : "GREEN", false),
                        new ChainNodeHealthDto("SPEC", counters.specsBlocked() > 0 || counters.specsInReview() > 0 ? "YELLOW" : "GREEN", true),
                        new ChainNodeHealthDto("ARCHITECTURE", "GREEN", false),
                        new ChainNodeHealthDto("DESIGN", "GREEN", false),
                        new ChainNodeHealthDto("TASKS", "GREEN", false),
                        new ChainNodeHealthDto("CODE", "GREEN", false),
                        new ChainNodeHealthDto("TEST", counters.specsApprovedAwaitingDownstream() > 0 ? "YELLOW" : "GREEN", false),
                        new ChainNodeHealthDto("DEPLOY", "GREEN", false),
                        new ChainNodeHealthDto("INCIDENT", hasCriticalIncident ? "RED" : "GREEN", false),
                        new ChainNodeHealthDto("LEARNING", "GREEN", false)
                ),
                BLOCKER_THRESHOLD_DAYS
        );
    }

    private int ageDays(java.time.Instant createdAt) {
        return (int) ChronoUnit.DAYS.between(createdAt, TeamSpaceConstants.REFERENCE_NOW);
    }
}
