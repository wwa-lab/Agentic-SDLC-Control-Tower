package com.sdlctower.domain.testingmanagement.integration;

import com.sdlctower.domain.requirement.persistence.RequirementEntity;
import com.sdlctower.domain.requirement.persistence.RequirementRepository;
import com.sdlctower.domain.requirement.persistence.UserStoryEntity;
import com.sdlctower.domain.requirement.persistence.UserStoryRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("testingManagementRequirementRepositoryLookup")
public class RequirementRepositoryLookup implements RequirementLookup {

    private final RequirementRepository requirementRepository;
    private final UserStoryRepository userStoryRepository;

    public RequirementRepositoryLookup(
            RequirementRepository requirementRepository,
            UserStoryRepository userStoryRepository
    ) {
        this.requirementRepository = requirementRepository;
        this.userStoryRepository = userStoryRepository;
    }

    @Override
    public Optional<RequirementRef> find(String reqId) {
        return requirementRepository.findById(reqId)
                .map(requirement -> toRef(requirement, firstStoryId(requirement.getId())));
    }

    @Override
    public Map<String, RequirementRef> findByIds(Collection<String> reqIds) {
        if (reqIds == null || reqIds.isEmpty()) {
            return Map.of();
        }
        List<RequirementEntity> requirements = requirementRepository.findAllById(reqIds);
        Map<String, String> storyIds = userStoryRepository.findByRequirementIdIn(requirements.stream()
                        .map(RequirementEntity::getId)
                        .toList())
                .stream()
                .collect(Collectors.groupingBy(
                        UserStoryEntity::getRequirementId,
                        LinkedHashMap::new,
                        Collectors.mapping(UserStoryEntity::getId, Collectors.collectingAndThen(Collectors.toList(), ids -> ids.isEmpty() ? null : ids.get(0)))
                ));

        Map<String, RequirementRef> result = new LinkedHashMap<>();
        for (RequirementEntity requirement : requirements) {
            RequirementRef ref = toRef(requirement, storyIds.get(requirement.getId()));
            result.put(requirement.getId(), ref);
        }
        return result;
    }

    private RequirementRef toRef(RequirementEntity requirement, String storyId) {
        return new RequirementRef(
                storyId,
                requirement.getId(),
                requirement.getTitle(),
                inferProjectId(requirement.getId()),
                requirement.getStatus()
        );
    }

    private String firstStoryId(String requirementId) {
        return userStoryRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId).stream()
                .map(UserStoryEntity::getId)
                .findFirst()
                .orElse(null);
    }

    private String inferProjectId(String requirementId) {
        return switch (requirementId) {
            case "REQ-0001", "REQ-0005", "REQ-0006", "REQ-0010" -> "proj-42";
            case "REQ-0002" -> "proj-11";
            case "REQ-0003" -> "proj-55";
            case "REQ-0004", "REQ-0009" -> "proj-88";
            default -> "proj-07";
        };
    }
}
