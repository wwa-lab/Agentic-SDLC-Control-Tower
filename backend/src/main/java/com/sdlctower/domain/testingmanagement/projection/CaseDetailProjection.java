package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.CaseDetailDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.IncidentChipDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RequirementChipDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.DraftOrigin;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.ReqLinkStatus;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCasePriority;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseState;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.TestCaseType;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.policy.TestingManagementException;
import com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("testingManagementCaseDetailProjection")
public class CaseDetailProjection {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;
    private final RequirementLookup requirementLookup;
    private final PlanHeaderProjection planHeaderProjection;
    private final TestingManagementReadSupport readSupport;

    public CaseDetailProjection(
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository,
            RequirementLookup requirementLookup,
            PlanHeaderProjection planHeaderProjection,
            TestingManagementReadSupport readSupport
    ) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
        this.requirementLookup = requirementLookup;
        this.planHeaderProjection = planHeaderProjection;
        this.readSupport = readSupport;
    }

    public CaseDetailDto load(String caseId) {
        TestCaseEntity testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> TestingManagementException.notFound("TM_CASE_NOT_FOUND", "Case not found: " + caseId));
        List<TestCaseReqLinkEntity> links = testCaseReqLinkRepository.findByCaseId(caseId);
        Map<String, RequirementRef> requirements = requirementLookup.findByIds(links.stream().map(TestCaseReqLinkEntity::getReqId).toList());

        return new CaseDetailDto(
                testCase.getId(),
                testCase.getPlanId(),
                testCase.getProjectId(),
                planHeaderProjection.load(testCase.getPlanId()).name(),
                testCase.getTitle(),
                TestCaseType.valueOf(testCase.getType()),
                TestCasePriority.valueOf(testCase.getPriority()),
                TestCaseState.valueOf(testCase.getState()),
                DraftOrigin.valueOf(testCase.getOrigin()),
                readSupport.memberRef(testCase.getOwnerMemberId()),
                readSupport.sanitizeMarkdown(testCase.getPreconditions()),
                readSupport.sanitizeMarkdown(testCase.getSteps()),
                readSupport.sanitizeMarkdown(testCase.getExpectedResult()),
                links.stream().map(link -> toChip(link, requirements.get(link.getReqId()))).toList(),
                List.of(new IncidentChipDto("inc-2401", "Payment timeout on callback", "P1", "/incidents/inc-2401")),
                testCase.getCreatedAt(),
                testCase.getUpdatedAt()
        );
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
}
