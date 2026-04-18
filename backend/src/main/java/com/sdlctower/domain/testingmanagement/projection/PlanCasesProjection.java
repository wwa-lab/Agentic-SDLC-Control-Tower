package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.PlanCaseRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RequirementChipDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.ReqLinkStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCasePriority;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseState;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseType;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestResultOutcome;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanCasesProjection")
public class PlanCasesProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final RequirementLookup requirementLookup;
    private final TestingManagementReadSupport readSupport;

    public PlanCasesProjection(
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository,
            TestCaseResultRepository testCaseResultRepository,
            RequirementLookup requirementLookup,
            TestingManagementReadSupport readSupport
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
        this.testCaseResultRepository = testCaseResultRepository;
        this.requirementLookup = requirementLookup;
        this.readSupport = readSupport;
    }

    public List<PlanCaseRowDto> load(String planId) {
        List<TestCaseEntity> cases = testCaseRepository.findByPlanIdOrderByCreatedAtAsc(planId);
        Map<String, List<TestCaseReqLinkEntity>> linksByCaseId = linksByCaseId(cases.stream().map(TestCaseEntity::getId).toList());
        Map<String, RequirementRef> requirements = requirementLookup.findByIds(linksByCaseId.values().stream()
                .flatMap(List::stream)
                .map(TestCaseReqLinkEntity::getReqId)
                .toList());
        Map<String, TestCaseResultEntity> latestResults = latestResultsByCaseIds(cases.stream().map(TestCaseEntity::getId).toList());

        return cases.stream()
                .map(testCase -> {
                    TestCaseResultEntity latest = latestResults.get(testCase.getId());
                    return new PlanCaseRowDto(
                            testCase.getId(),
                            testCase.getPlanId(),
                            testCase.getTitle(),
                            TestCaseType.valueOf(testCase.getType()),
                            TestCasePriority.valueOf(testCase.getPriority()),
                            TestCaseState.valueOf(testCase.getState()),
                            linksByCaseId.getOrDefault(testCase.getId(), List.of()).stream()
                                    .map(link -> toChip(link, requirements.get(link.getReqId())))
                                    .toList(),
                            latest == null ? null : TestResultOutcome.valueOf(latest.getOutcome()),
                            latest == null ? null : latest.getCreatedAt()
                    );
                })
                .toList();
    }

    private RequirementChipDto toChip(TestCaseReqLinkEntity link, RequirementRef ref) {
        ReqLinkStatus linkStatus = ReqLinkStatus.valueOf(link.getLinkStatus());
        return new RequirementChipDto(
                link.getReqId(),
                ref == null ? null : ref.storyId(),
                ref == null ? null : ref.title(),
                ref == null ? null : ref.projectId(),
                linkStatus,
                readSupport.resolveChipColor(linkStatus, ref),
                "/requirements/" + link.getReqId()
        );
    }

    private Map<String, List<TestCaseReqLinkEntity>> linksByCaseId(Collection<String> caseIds) {
        Map<String, List<TestCaseReqLinkEntity>> byCase = new LinkedHashMap<>();
        for (TestCaseReqLinkEntity link : testCaseReqLinkRepository.findByCaseIdIn(caseIds)) {
            byCase.computeIfAbsent(link.getCaseId(), key -> new java.util.ArrayList<>()).add(link);
        }
        return byCase;
    }

    private Map<String, TestCaseResultEntity> latestResultsByCaseIds(Collection<String> caseIds) {
        Map<String, TestCaseResultEntity> latest = new LinkedHashMap<>();
        for (TestCaseResultEntity result : testCaseResultRepository.findByCaseIdInOrderByCreatedAtDesc(caseIds)) {
            latest.putIfAbsent(result.getCaseId(), result);
        }
        return latest;
    }
}
