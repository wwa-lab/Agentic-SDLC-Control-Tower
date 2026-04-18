package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CoverageRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanCaseRefDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.TraceabilityReqRowDto;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("testingManagementTraceabilityReqRowsProjection")
public class TraceabilityReqRowsProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final RequirementLookup requirementLookup;
    private final PlanCoverageProjection planCoverageProjection;
    private final PlanHeaderProjection planHeaderProjection;
    private final TestingManagementReadSupport readSupport;

    public TraceabilityReqRowsProjection(
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository,
            TestCaseResultRepository testCaseResultRepository,
            RequirementLookup requirementLookup,
            PlanCoverageProjection planCoverageProjection,
            PlanHeaderProjection planHeaderProjection,
            TestingManagementReadSupport readSupport
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
        this.testCaseResultRepository = testCaseResultRepository;
        this.requirementLookup = requirementLookup;
        this.planCoverageProjection = planCoverageProjection;
        this.planHeaderProjection = planHeaderProjection;
        this.readSupport = readSupport;
    }

    public List<TraceabilityReqRowDto> load(String workspaceId) {
        List<TestCaseEntity> cases = testCaseRepository.findAll().stream()
                .filter(testCase -> workspaceId.equals(testCase.getWorkspaceId()))
                .filter(testCase -> "ACTIVE".equals(testCase.getState()))
                .toList();
        Map<String, TestCaseEntity> casesById = cases.stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
        List<TestCaseReqLinkEntity> links = testCaseReqLinkRepository.findByCaseIdIn(casesById.keySet());
        Map<String, RequirementRef> requirements = requirementLookup.findByIds(links.stream().map(TestCaseReqLinkEntity::getReqId).toList());
        Map<String, TestCaseResultEntity> latestResults = latestResultsByCaseIds(casesById.keySet());
        Map<String, List<CoverageRowDto>> coverageByPlan = new LinkedHashMap<>();
        for (TestCaseEntity testCase : cases) {
            coverageByPlan.computeIfAbsent(testCase.getPlanId(), planCoverageProjection::load);
        }

        Map<String, List<TestCaseReqLinkEntity>> linksByReq = new LinkedHashMap<>();
        for (TestCaseReqLinkEntity link : links) {
            linksByReq.computeIfAbsent(link.getReqId(), key -> new ArrayList<>()).add(link);
        }

        List<TraceabilityReqRowDto> rows = new ArrayList<>();
        for (Map.Entry<String, List<TestCaseReqLinkEntity>> entry : linksByReq.entrySet()) {
            RequirementRef ref = requirements.get(entry.getKey());
            List<PlanCaseRefDto> caseRefs = entry.getValue().stream()
                    .map(link -> {
                        TestCaseEntity testCase = casesById.get(link.getCaseId());
                        TestCaseResultEntity latest = latestResults.get(link.getCaseId());
                        return new PlanCaseRefDto(
                                testCase.getId(),
                                testCase.getTitle(),
                                testCase.getPlanId(),
                                planHeaderProjection.load(testCase.getPlanId()).name(),
                                latest == null ? null : com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome.valueOf(latest.getOutcome()),
                                latest == null ? null : latest.getCreatedAt()
                        );
                    })
                    .toList();
            Instant latestRunAt = caseRefs.stream()
                    .map(PlanCaseRefDto::lastRunAt)
                    .filter(value -> value != null)
                    .max(Instant::compareTo)
                    .orElse(null);
            com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus status = entry.getValue().stream()
                    .map(link -> coverageByPlan.getOrDefault(casesById.get(link.getCaseId()).getPlanId(), List.of()).stream()
                            .filter(row -> row.reqId().equals(link.getReqId()))
                            .findFirst()
                            .map(CoverageRowDto::aggregateStatus)
                            .orElse(com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus.GREY))
                    .findFirst()
                    .orElse(com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.CoverageStatus.GREY);
            rows.add(new TraceabilityReqRowDto(
                    entry.getKey(),
                    ref == null ? "Unknown requirement" : ref.title(),
                    ref == null ? null : ref.storyId(),
                    ref == null ? null : ref.projectId(),
                    ref == null || ref.projectId() == null ? "Unknown project" : readSupport.projectName(ref.projectId()),
                    entry.getValue().size(),
                    (int) caseRefs.stream().map(PlanCaseRefDto::planId).distinct().count(),
                    status,
                    latestRunAt,
                    caseRefs
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
