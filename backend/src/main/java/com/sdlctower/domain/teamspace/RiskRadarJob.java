package com.sdlctower.domain.teamspace;

import com.sdlctower.domain.teamspace.persistence.RiskSignalEntity;
import com.sdlctower.domain.teamspace.persistence.RiskSignalRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RiskRadarJob {

    private final RiskSignalRepository riskSignalRepository;

    public RiskRadarJob(RiskSignalRepository riskSignalRepository) {
        this.riskSignalRepository = riskSignalRepository;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void refresh() {
        if (riskSignalRepository.count() > 0) {
            return;
        }

        riskSignalRepository.save(RiskSignalEntity.create(
                "RISK-AUTO-0001",
                "ws-default-001",
                "APPROVAL",
                "HIGH",
                "SPEC",
                "SPEC-0088",
                "Spec approvals pending > 3d",
                "Auto-refresh restored baseline risk coverage for Team Space",
                "Review approvals",
                "/platform?view=approvals&workspaceId=ws-default-001",
                "risk-refresh",
                "risk-refresh-0001",
                Instant.now(),
                null
        ));
    }
}
