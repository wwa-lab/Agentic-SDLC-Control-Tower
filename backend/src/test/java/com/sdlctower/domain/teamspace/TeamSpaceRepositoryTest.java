package com.sdlctower.domain.teamspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.sdlctower.domain.teamspace.persistence.MetricSnapshotRepository;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class TeamSpaceRepositoryTest {

    @Autowired
    private RiskSignalRepository riskSignalRepository;

    @Autowired
    private MetricSnapshotRepository metricSnapshotRepository;

    @Test
    void seedRiskSignalsAreReadableFromH2() {
        assertEquals(3, riskSignalRepository.findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc("ws-default-001").size());
        assertFalse(riskSignalRepository.findByWorkspaceIdAndResolvedAtIsNullOrderByDetectedAtDesc("ws-legacy-001").isEmpty());
    }

    @Test
    void seedMetricSnapshotsAreReadableFromH2() {
        assertEquals(10, metricSnapshotRepository.findByWorkspaceIdOrderBySnapshotAtDesc("ws-default-001").size());
        assertEquals(1, metricSnapshotRepository.findByWorkspaceIdOrderBySnapshotAtDesc("ws-legacy-001").size());
    }
}
