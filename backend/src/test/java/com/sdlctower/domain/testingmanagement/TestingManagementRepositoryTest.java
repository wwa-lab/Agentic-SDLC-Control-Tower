package com.sdlctower.domain.testingmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sdlctower.domain.testingmanagement.persistence.TestCaseRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestCaseReqLinkRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestPlanRepository;
import com.sdlctower.domain.testingmanagement.persistence.TestRunRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class TestingManagementRepositoryTest {

    @Autowired
    private TestPlanRepository testPlanRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestCaseReqLinkRepository testCaseReqLinkRepository;

    @Autowired
    private TestRunRepository testRunRepository;

    @Test
    void planAndCaseSeedsAreReadable() {
        assertEquals(4, testPlanRepository.findByWorkspaceIdOrderByUpdatedAtDesc("ws-default-001").size());
        assertEquals(3, testCaseRepository.findByPlanIdOrderByCreatedAtAsc("plan-auth-001").size());
        assertFalse(testCaseReqLinkRepository.findByCaseId("case-color-1102").isEmpty());
    }

    @Test
    void planRunsAreOrderedMostRecentFirst() {
        assertEquals("run-auth-001", testRunRepository.findByPlanIdOrderByStartedAtDesc("plan-auth-001").get(0).getId());
    }
}
