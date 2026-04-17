package com.sdlctower.domain.projectspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sdlctower.domain.projectspace.persistence.DeploymentRepository;
import com.sdlctower.domain.projectspace.persistence.EnvironmentRepository;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class ProjectSpaceRepositoryTest {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private RiskSignalRepository riskSignalRepository;

    @Test
    void seedMilestonesAreReadableFromH2() {
        assertEquals(3, milestoneRepository.findByProjectIdOrderByOrderingAsc("proj-42").size());
        assertFalse(milestoneRepository.findByProjectIdOrderByOrderingAsc("proj-88").isEmpty());
    }

    @Test
    void seedEnvironmentsExposeLatestDeployment() {
        assertEquals(3, environmentRepository.findByProjectIdOrderByLabelAsc("proj-42").size());
        assertEquals(
                "v2.4.0-rc.4",
                deploymentRepository.findFirstByEnvironmentIdOrderByDeployedAtDesc("ENV-P42-STAGE").orElseThrow().getVersionRef()
        );
    }

    @Test
    void projectScopedRisksStayIsolatedFromWorkspaceRadar() {
        assertEquals(2, riskSignalRepository.findByProjectIdAndResolvedAtIsNullOrderByDetectedAtDesc("proj-42").size());
        assertEquals(3, riskSignalRepository.findByWorkspaceIdAndProjectIdIsNullAndResolvedAtIsNullOrderByDetectedAtDesc("ws-default-001").size());
    }
}
