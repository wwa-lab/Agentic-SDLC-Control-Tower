package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseState;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import com.sdlctower.domain.testingmanagement.policy.CoverageCalculator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanCoverageProjection")
public class PlanCoverageProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final RequirementLookup requirementLookup;
    private final CoverageCalculator coverageCalculator;

    public PlanCoverageProjection(
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository,
            TestCaseResultRepository testCaseResultRepository,
            RequirementLookup requirementLookup,
            CoverageCalculator coverageCalculator
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
        this.testCaseResultRepository = testCaseResultRepository;
        this.requirementLookup = requirementLookup;
        this.coverageCalculator = coverageCalculator;
    }

    public List<CoverageRowDto> load(String planId) {
        List<TestCaseEntity> activeCases = testCaseRepository.findByPlanIdAndStateOrderByCreatedAtAsc(planId, TestCaseState.ACTIVE.name());
        if (activeCases.isEmpty()) {
            return List.of();
        }

        Map<String, TestCaseEntity> casesById = activeCases.stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
        List<TestCaseReqLinkEntity> links = testCaseReqLinkRepository.findByCaseIdIn(casesById.keySet());
        Map<String, List<TestCaseReqLinkEntity>> linksByReqId = new LinkedHashMap<>();
        for (TestCaseReqLinkEntity link : links) {
            linksByReqId.computeIfAbsent(link.getReqId(), key -> new ArrayList<>()).add(link);
        }

        Map<String, RequirementRef> requirements = requirementLookup.findByIds(linksByReqId.keySet());
        Map<String, TestCaseResultEntity> latestResults = latestResultsByCaseIds(casesById.keySet());

        List<CoverageRowDto> rows = new ArrayList<>();
        for (Map.Entry<String, List<TestCaseReqLinkEntity>> entry : linksByReqId.entrySet()) {
            List<TestCaseResultEntity> relevantResults = entry.getValue().stream()
                    .map(TestCaseReqLinkEntity::getCaseId)
                    .map(latestResults::get)
                    .filter(result -> result != null)
                    .toList();
            RequirementRef requirement = requirements.get(entry.getKey());
            Instant mostRecentAt = relevantResults.stream()
                    .map(TestCaseResultEntity::getCreatedAt)
                    .max(Instant::compareTo)
                    .orElse(null);
            rows.add(new CoverageRowDto(
                    entry.getKey(),
                    requirement == null ? "Unknown requirement" : requirement.title(),
                    entry.getValue().size(),
                    coverageCalculator.forLatestResults(relevantResults),
                    mostRecentAt
            ));
        }
        return rows;
    }

    private Map<String, TestCaseResultEntity> latestResultsByCaseIds(Collection<String> caseIds) {
        Map<String, TestCaseResultEntity> latest = new LinkedHashMap<>();
        for (TestCaseResultEntity result : testCaseResultRepository.findByCaseIdInOrderByCreatedAtDesc(caseIds)) {
            latest.putIfAbsent(result.getCaseId(), result);
        }
        return latest;
    }
}
