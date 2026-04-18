package com.sdlctower.domain.testingmanagement.projection;

import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RequirementChipDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementDtos.RunCoverageDto;
import com.sdlctower.domain.testingmanagement.dto.TestingManagementEnums.ReqLinkStatus;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup;
import com.sdlctower.domain.testingmanagement.integration.RequirementLookup.RequirementRef;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkEntity;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseResultRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("testingManagementRunCoverageProjection")
public class RunCoverageProjection {

    private final TestCaseResultRepository testCaseResultRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseReqLinkRepository testCaseReqLinkRepository;
    private final RequirementLookup requirementLookup;
    private final com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport readSupport;

    public RunCoverageProjection(
            TestCaseResultRepository testCaseResultRepository,
            TestCaseRepository testCaseRepository,
            TestCaseReqLinkRepository testCaseReqLinkRepository,
            RequirementLookup requirementLookup,
            com.sdlctower.domain.testingmanagement.service.TestingManagementReadSupport readSupport
    ) {
        this.testCaseResultRepository = testCaseResultRepository;
        this.testCaseRepository = testCaseRepository;
        this.testCaseReqLinkRepository = testCaseReqLinkRepository;
        this.requirementLookup = requirementLookup;
        this.readSupport = readSupport;
    }

    public RunCoverageDto load(String runId) {
        List<String> caseIds = testCaseResultRepository.findByRunIdOrderByCreatedAtAsc(runId).stream()
                .map(com.sdlctower.domain.testingmanagement.persistence.TestCaseResultEntity::getCaseId)
                .distinct()
                .toList();
        Map<String, TestCaseEntity> cases = testCaseRepository.findByIdIn(caseIds).stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
        List<TestCaseReqLinkEntity> links = testCaseReqLinkRepository.findByCaseIdIn(caseIds).stream()
                .filter(link -> {
                    TestCaseEntity testCase = cases.get(link.getCaseId());
                    return testCase != null && "ACTIVE".equals(testCase.getState());
                })
                .toList();
        Map<String, RequirementRef> requirements = requirementLookup.findByIds(links.stream().map(TestCaseReqLinkEntity::getReqId).toList());
        List<RequirementChipDto> chips = links.stream()
                .map(link -> {
                    RequirementRef ref = requirements.get(link.getReqId());
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
                })
                .distinct()
                .toList();
        return new RunCoverageDto(chips.size(), chips);
    }
}
