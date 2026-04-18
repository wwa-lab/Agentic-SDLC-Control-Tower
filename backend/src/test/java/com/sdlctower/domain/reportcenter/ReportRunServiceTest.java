package com.sdlctower.domain.reportcenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunRequestDto;
import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.TimeRangeDto;
import com.sdlctower.domain.reportcenter.service.ReportRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class ReportRunServiceTest {

    @Autowired
    private ReportRunService reportRunService;

    @Test
    void runsAllFiveReportsAgainstSeedData() {
        for (String reportKey : java.util.List.of("eff.lead-time", "eff.cycle-time", "eff.throughput", "eff.wip", "eff.flow-efficiency")) {
            ReportRunService.RunExecution execution = reportRunService.run(
                    reportKey,
                    new ReportRunRequestDto("workspace", java.util.List.of("ws-default-001"), new TimeRangeDto("last30d", null, null), defaultGrouping(reportKey), java.util.Map.of())
            );
            assertEquals(reportKey, execution.result().reportKey());
            assertNotNull(execution.result().headline());
            assertNotNull(execution.result().series());
            assertNotNull(execution.result().drilldown());
        }
    }

    private String defaultGrouping(String reportKey) {
        return switch (reportKey) {
            case "eff.lead-time" -> "team";
            case "eff.cycle-time" -> "stage";
            case "eff.throughput" -> "week-team";
            case "eff.wip" -> "stage-team";
            default -> "stage";
        };
    }
}
