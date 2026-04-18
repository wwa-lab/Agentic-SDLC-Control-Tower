package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.AiDraftRowDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseState;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementPlanDraftInboxProjection")
public class PlanDraftInboxProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;

    public PlanDraftInboxProjection(
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
    }

    public List<AiDraftRowDto> load(String planId) {
        return testCaseRepository.findByPlanIdAndStateOrderByCreatedAtAsc(planId, TestCaseState.DRAFT.name()).stream()
                .filter(testCase -> "AI_DRAFT".equals(testCase.getOrigin()))
                .map(testCase -> new AiDraftRowDto(
                        testCase.getId(),
                        testCase.getTitle(),
                        testCaseReqLinkRepository.findByCaseId(testCase.getId()).stream().findFirst().map(TestCaseReqLinkEntity::getReqId).orElse(null),
                        "test-case-drafter@1.0.0",
                        testCase.getCreatedAt(),
                        true
                ))
                .toList();
    }
}
