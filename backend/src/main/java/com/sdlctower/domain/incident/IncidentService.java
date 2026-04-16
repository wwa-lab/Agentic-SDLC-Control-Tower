package com.sdlctower.domain.incident;

import com.sdlctower.domain.incident.dto.ActionApprovalResultDto;
import com.sdlctower.domain.incident.dto.AiLearningDto;
import com.sdlctower.domain.incident.dto.DiagnosisEntryDto;
import com.sdlctower.domain.incident.dto.DiagnosisFeedDto;
import com.sdlctower.domain.incident.dto.GovernanceDto;
import com.sdlctower.domain.incident.dto.GovernanceEntryDto;
import com.sdlctower.domain.incident.dto.IncidentActionDto;
import com.sdlctower.domain.incident.dto.IncidentActionsDto;
import com.sdlctower.domain.incident.dto.IncidentDetailDto;
import com.sdlctower.domain.incident.dto.IncidentHeaderDto;
import com.sdlctower.domain.incident.dto.IncidentListDto;
import com.sdlctower.domain.incident.dto.IncidentListItemDto;
import com.sdlctower.domain.incident.dto.RootCauseHypothesisDto;
import com.sdlctower.domain.incident.dto.SdlcChainDto;
import com.sdlctower.domain.incident.dto.SdlcChainLinkDto;
import com.sdlctower.domain.incident.dto.SeverityDistributionDto;
import com.sdlctower.domain.incident.dto.SkillExecutionDto;
import com.sdlctower.domain.incident.dto.SkillTimelineDto;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    /**
     * In-memory mutable state for approve/reject actions (Phase B seed data).
     * Replaced by JPA persistence in production.
     */
    private final Map<String, String> actionStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<GovernanceEntryDto>> incidentGovernance = new ConcurrentHashMap<>();

    public IncidentListDto getIncidentList() {
        return new IncidentListDto(
                new SeverityDistributionDto(1, 2, 2, 0),
                buildIncidentList()
        );
    }

    public IncidentDetailDto getIncidentDetail(String incidentId) {
        var builder = DETAIL_BUILDERS.get(incidentId);
        if (builder == null) {
            throw new ResourceNotFoundException("Incident not found: " + incidentId);
        }
        return builder.build(this, incidentId);
    }

    public ActionApprovalResultDto approveAction(String incidentId, String actionId) {
        validateIncidentExists(incidentId);
        actionStatuses.put(actionId, "approved");
        var entry = new GovernanceEntryDto(
                "leo.chen", Instant.now().toString(), "approve", null, null);
        incidentGovernance.computeIfAbsent(incidentId, k -> new ArrayList<>()).add(entry);
        return new ActionApprovalResultDto(actionId, "approved", entry);
    }

    public ActionApprovalResultDto rejectAction(String incidentId, String actionId, String reason) {
        validateIncidentExists(incidentId);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        actionStatuses.put(actionId, "rejected");
        var entry = new GovernanceEntryDto(
                "leo.chen", Instant.now().toString(), "reject", reason, null);
        incidentGovernance.computeIfAbsent(incidentId, k -> new ArrayList<>()).add(entry);
        return new ActionApprovalResultDto(actionId, "rejected", entry);
    }

    private void validateIncidentExists(String incidentId) {
        if (!DETAIL_BUILDERS.containsKey(incidentId)) {
            throw new ResourceNotFoundException("Incident not found: " + incidentId);
        }
    }

    private String getActionStatus(String actionId, String defaultStatus) {
        return actionStatuses.getOrDefault(actionId, defaultStatus);
    }

    private List<GovernanceEntryDto> getGovernanceEntries(String incidentId, List<GovernanceEntryDto> seed) {
        var dynamic = incidentGovernance.getOrDefault(incidentId, List.of());
        if (dynamic.isEmpty()) return seed;
        var merged = new ArrayList<>(seed);
        merged.addAll(dynamic);
        return merged;
    }

    private <T> SectionResultDto<T> loadSection(String name, Supplier<T> builder) {
        try {
            return SectionResultDto.ok(builder.get());
        } catch (Exception e) {
            log.error("Failed to load incident section [{}]: {}", name, e.getMessage(), e);
            return SectionResultDto.fail("Failed to load " + name);
        }
    }

    // ── Per-incident detail builders ──

    @FunctionalInterface
    private interface DetailBuilder {
        IncidentDetailDto build(IncidentService svc, String incidentId);
    }

    private static final Map<String, DetailBuilder> DETAIL_BUILDERS = Map.of(
            "INC-0422", IncidentService::buildDetail0422,
            "INC-0421", IncidentService::buildDetail0421,
            "INC-0420", IncidentService::buildDetail0420,
            "INC-0419", IncidentService::buildDetail0419,
            "INC-0418", IncidentService::buildDetail0418
    );

    // ── INC-0422: P1, PENDING_APPROVAL, AI ──

    private static IncidentDetailDto buildDetail0422(IncidentService svc, String id) {
        return new IncidentDetailDto(
                svc.loadSection("header", () -> new IncidentHeaderDto(
                        "INC-0422", "API Gateway Latency Spike (>500ms)", "P1", "PENDING_APPROVAL",
                        "AI", "Approval", "Level2_SuggestApprove",
                        "2026-04-16T09:40:00Z", "2026-04-16T09:40:02Z", null, "PT1H23M")),
                svc.loadSection("diagnosis", () -> new DiagnosisFeedDto(
                        List.of(
                                new DiagnosisEntryDto("09:41:02", "Analyzing k8s ingress logs for anomalous patterns...", "analysis"),
                                new DiagnosisEntryDto("09:41:05", "Detected 3x increase in p99 latency starting at 09:38", "finding"),
                                new DiagnosisEntryDto("09:41:08", "Correlating with recent deployment DEP-041 (v2.4.0) at 09:35", "analysis"),
                                new DiagnosisEntryDto("09:41:12", "Pattern identified: SSL handshake timeout causing connection queuing", "finding"),
                                new DiagnosisEntryDto("09:41:15", "Root cause: New TLS config in v2.4.0 increases handshake time by 300ms under load", "conclusion"),
                                new DiagnosisEntryDto("09:41:18", "SUGGESTION: Scale pod replicas from 3 to 5 to absorb load while TLS config is patched", "suggestion")),
                        new RootCauseHypothesisDto("TLS configuration change in deployment v2.4.0 causes SSL handshake timeout under load", "High"),
                        List.of("api-gateway", "ingress-controller", "product-service"))),
                svc.loadSection("skillTimeline", () -> new SkillTimelineDto(List.of(
                        new SkillExecutionDto("incident-detection", "2026-04-16T09:40:00Z", "2026-04-16T09:40:02Z", "completed", "Anomaly signal: p99 latency > 500ms on api-gateway", "Incident INC-0422 created, priority P1"),
                        new SkillExecutionDto("incident-correlation", "2026-04-16T09:40:03Z", "2026-04-16T09:40:08Z", "completed", "INC-0422 signals + recent change log", "Correlated with DEP-041 (v2.4.0) deployed at 09:35"),
                        new SkillExecutionDto("incident-diagnosis", "2026-04-16T09:41:00Z", "2026-04-16T09:41:15Z", "completed", "INC-0422 + correlation data + ingress logs", "Root cause: TLS config change causing handshake timeout"),
                        new SkillExecutionDto("incident-remediation", "2026-04-16T09:41:16Z", null, "pending_approval", "Root cause + affected components", "Proposed: Scale replicas 3→5 (requires approval per policy)")))),
                svc.loadSection("actions", () -> new IncidentActionsDto(List.of(
                        new IncidentActionDto("ACT-001", "Scale api-gateway pod replicas from 3 to 5", "requires_approval",
                                svc.getActionStatus("ACT-001", "pending"),
                                "2026-04-16T09:41:18Z", "Increases resource allocation by 67%; absorbs current load spike; no downtime",
                                true, "POL-003: Infrastructure scaling requires approval for >50% capacity change")))),
                svc.loadSection("governance", () -> new GovernanceDto(svc.getGovernanceEntries(id, List.of()))),
                svc.loadSection("sdlcChain", () -> new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("spec", "SPEC-089", "API Gateway TLS Configuration Spec", "/requirements"),
                        new SdlcChainLinkDto("code", "MR-1234", "feat: update TLS config for mTLS support", "/code"),
                        new SdlcChainLinkDto("deploy", "DEP-041", "Release v2.4.0 to production", "/deployment")))),
                new SectionResultDto<>(null, null)
        );
    }

    // ── INC-0421: P2, EXECUTING, AI ──

    private static IncidentDetailDto buildDetail0421(IncidentService svc, String id) {
        return new IncidentDetailDto(
                svc.loadSection("header", () -> new IncidentHeaderDto(
                        "INC-0421", "Database Connection Pool Exhaustion", "P2", "EXECUTING",
                        "AI", "Approval", "Level2_SuggestApprove",
                        "2026-04-16T08:15:00Z", "2026-04-16T08:15:03Z", null, "PT2H48M")),
                svc.loadSection("diagnosis", () -> new DiagnosisFeedDto(
                        List.of(
                                new DiagnosisEntryDto("08:16:00", "Analyzing HikariCP pool metrics...", "analysis"),
                                new DiagnosisEntryDto("08:16:05", "Active connections at 100% capacity (50/50)", "finding"),
                                new DiagnosisEntryDto("08:16:10", "Long-running queries detected in order-service", "finding"),
                                new DiagnosisEntryDto("08:16:15", "SUGGESTION: Kill idle connections and increase pool max to 80", "suggestion")),
                        new RootCauseHypothesisDto("Long-running queries from batch job saturated connection pool", "Medium"),
                        List.of("order-service", "postgres-primary"))),
                svc.loadSection("skillTimeline", () -> new SkillTimelineDto(List.of(
                        new SkillExecutionDto("incident-detection", "2026-04-16T08:15:00Z", "2026-04-16T08:15:03Z", "completed", "Connection pool exhaustion alert", "INC-0421 created, P2"),
                        new SkillExecutionDto("incident-diagnosis", "2026-04-16T08:16:00Z", "2026-04-16T08:16:15Z", "completed", "Pool metrics + query log", "Batch job causing pool saturation"),
                        new SkillExecutionDto("incident-remediation", "2026-04-16T08:17:00Z", "2026-04-16T08:17:30Z", "completed", "Kill idle + resize pool", "Executing approved remediation")))),
                svc.loadSection("actions", () -> new IncidentActionsDto(List.of(
                        new IncidentActionDto("ACT-002", "Increase connection pool max from 50 to 80", "requires_approval",
                                svc.getActionStatus("ACT-002", "executing"),
                                "2026-04-16T08:17:00Z", "Temporary pool increase; monitor for 1h then reassess", true, null)))),
                svc.loadSection("governance", () -> new GovernanceDto(svc.getGovernanceEntries(id, List.of(
                        new GovernanceEntryDto("alex.kim", "2026-04-16T08:16:45Z", "approve", null, null))))),
                svc.loadSection("sdlcChain", () -> new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("code", "MR-1198", "feat: batch order reconciliation job", "/code")))),
                new SectionResultDto<>(null, null)
        );
    }

    // ── INC-0420: P3, RESOLVED, Hybrid (with learning) ──

    private static IncidentDetailDto buildDetail0420(IncidentService svc, String id) {
        return new IncidentDetailDto(
                svc.loadSection("header", () -> new IncidentHeaderDto(
                        "INC-0420", "Cache Miss Rate Spike in Product Service", "P3", "RESOLVED",
                        "Hybrid", "Manual", "Level1_Manual",
                        "2026-04-15T14:30:00Z", "2026-04-15T14:32:00Z", "2026-04-15T18:42:00Z", "PT4H12M")),
                svc.loadSection("diagnosis", () -> new DiagnosisFeedDto(
                        List.of(
                                new DiagnosisEntryDto("14:31:00", "Analyzing Redis cache hit/miss ratios...", "analysis"),
                                new DiagnosisEntryDto("14:31:08", "Cache miss rate increased from 5% to 42% at 14:28", "finding"),
                                new DiagnosisEntryDto("14:31:15", "SUGGESTION: Flush stale cache keys and increase TTL for product catalog", "suggestion")),
                        new RootCauseHypothesisDto("Bulk product catalog update invalidated cache without warming", "Medium"),
                        List.of("product-service", "redis-cluster"))),
                svc.loadSection("skillTimeline", () -> new SkillTimelineDto(List.of(
                        new SkillExecutionDto("incident-detection", "2026-04-15T14:30:00Z", "2026-04-15T14:30:05Z", "completed", "Cache miss alert threshold breached", "INC-0420 created, P3"),
                        new SkillExecutionDto("incident-diagnosis", "2026-04-15T14:31:00Z", "2026-04-15T14:31:15Z", "completed", "Redis metrics + change log", "Bulk catalog update identified as trigger")))),
                svc.loadSection("actions", () -> new IncidentActionsDto(List.of(
                        new IncidentActionDto("ACT-010", "Flush stale product cache keys", "requires_approval", "executed",
                                "2026-04-15T14:35:00Z", "Brief 2-3s latency spike during cache rebuild", false, null)))),
                svc.loadSection("governance", () -> new GovernanceDto(svc.getGovernanceEntries(id, List.of(
                        new GovernanceEntryDto("sarah.chen", "2026-04-15T14:34:00Z", "approve", null, null),
                        new GovernanceEntryDto("sarah.chen", "2026-04-15T18:40:00Z", "override", "Manual cache warming applied; closing via override", null))))),
                svc.loadSection("sdlcChain", () -> new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("code", "MR-1201", "feat: bulk product catalog sync", "/code")))),
                SectionResultDto.ok(new AiLearningDto(
                        "Bulk catalog update invalidated 12,000 cache keys simultaneously without warming",
                        "Large-scale cache invalidation without pre-warming causes cascading miss spikes",
                        List.of("Implement cache warming step in bulk update pipeline",
                                "Add circuit breaker for cache miss rate > 30%",
                                "Create runbook for manual cache warming procedure"),
                        true))
        );
    }

    // ── INC-0419: P3, CLOSED, AI (with learning) ──

    private static IncidentDetailDto buildDetail0419(IncidentService svc, String id) {
        return new IncidentDetailDto(
                svc.loadSection("header", () -> new IncidentHeaderDto(
                        "INC-0419", "Certificate Expiry Warning (7 days)", "P3", "CLOSED",
                        "AI", "Auto", "Level3_AutoAudit",
                        "2026-04-14T10:00:00Z", "2026-04-14T10:00:01Z", "2026-04-14T10:45:00Z", "PT0H45M")),
                svc.loadSection("diagnosis", () -> new DiagnosisFeedDto(
                        List.of(
                                new DiagnosisEntryDto("10:00:05", "Certificate expiry scan triggered", "analysis"),
                                new DiagnosisEntryDto("10:00:10", "api-gateway.example.com cert expires in 7 days", "finding"),
                                new DiagnosisEntryDto("10:00:15", "Auto-renewing via Let's Encrypt ACME flow", "conclusion")),
                        new RootCauseHypothesisDto("Certificate approaching expiry; auto-renewal initiated", "High"),
                        List.of("api-gateway"))),
                svc.loadSection("skillTimeline", () -> new SkillTimelineDto(List.of(
                        new SkillExecutionDto("incident-detection", "2026-04-14T10:00:00Z", "2026-04-14T10:00:01Z", "completed", "Cert expiry monitoring", "INC-0419 created, P3"),
                        new SkillExecutionDto("incident-remediation", "2026-04-14T10:00:15Z", "2026-04-14T10:44:00Z", "completed", "ACME renewal flow", "Certificate renewed successfully")))),
                svc.loadSection("actions", () -> new IncidentActionsDto(List.of(
                        new IncidentActionDto("ACT-020", "Auto-renew certificate via ACME", "automated", "executed",
                                "2026-04-14T10:00:15Z", "Zero-downtime cert rotation", false, null)))),
                svc.loadSection("governance", () -> new GovernanceDto(svc.getGovernanceEntries(id, List.of()))),
                svc.loadSection("sdlcChain", () -> new SdlcChainDto(List.of())),
                SectionResultDto.ok(new AiLearningDto(
                        "Certificate nearing expiry due to renewal job being paused during maintenance window",
                        "Maintenance windows can block automated cert renewal",
                        List.of("Exclude cert renewal from maintenance freeze windows",
                                "Add 14-day early warning threshold instead of 7 days"),
                        true))
        );
    }

    // ── INC-0418: P2, LEARNING, AI ──

    private static IncidentDetailDto buildDetail0418(IncidentService svc, String id) {
        return new IncidentDetailDto(
                svc.loadSection("header", () -> new IncidentHeaderDto(
                        "INC-0418", "Memory Leak in Notification Worker", "P2", "LEARNING",
                        "AI", "Approval", "Level2_SuggestApprove",
                        "2026-04-13T16:20:00Z", "2026-04-13T16:20:05Z", "2026-04-13T22:50:00Z", "PT6H30M")),
                svc.loadSection("diagnosis", () -> new DiagnosisFeedDto(
                        List.of(
                                new DiagnosisEntryDto("16:21:00", "Analyzing heap dump from notification-worker pod...", "analysis"),
                                new DiagnosisEntryDto("16:21:15", "Heap usage growing linearly: 2GB/hr, OOM projected in 4h", "finding"),
                                new DiagnosisEntryDto("16:21:30", "Leak traced to unclosed WebSocket connections in retry loop", "conclusion"),
                                new DiagnosisEntryDto("16:21:35", "SUGGESTION: Restart worker pod and deploy fix from MR-1210", "suggestion")),
                        new RootCauseHypothesisDto("WebSocket connections not closed in notification retry loop", "High"),
                        List.of("notification-worker"))),
                svc.loadSection("skillTimeline", () -> new SkillTimelineDto(List.of(
                        new SkillExecutionDto("incident-detection", "2026-04-13T16:20:00Z", "2026-04-13T16:20:05Z", "completed", "Memory usage alert > 80%", "INC-0418 created, P2"),
                        new SkillExecutionDto("incident-diagnosis", "2026-04-13T16:21:00Z", "2026-04-13T16:21:35Z", "completed", "Heap dump analysis", "WebSocket leak in retry loop"),
                        new SkillExecutionDto("incident-remediation", "2026-04-13T16:25:00Z", "2026-04-13T16:26:00Z", "completed", "Restart pod + deploy fix", "Worker restarted, fix deployed"),
                        new SkillExecutionDto("incident-learning", "2026-04-13T22:50:00Z", null, "running", "Post-incident analysis", "Generating prevention recommendations")))),
                svc.loadSection("actions", () -> new IncidentActionsDto(List.of(
                        new IncidentActionDto("ACT-030", "Restart notification-worker pod", "requires_approval", "executed",
                                "2026-04-13T16:25:00Z", "Service interruption for ~30s during restart", true, null),
                        new IncidentActionDto("ACT-031", "Deploy hotfix MR-1210", "requires_approval", "executed",
                                "2026-04-13T16:25:30Z", "Fixes WebSocket connection leak; rolling deploy", false, null)))),
                svc.loadSection("governance", () -> new GovernanceDto(svc.getGovernanceEntries(id, List.of(
                        new GovernanceEntryDto("mike.ross", "2026-04-13T16:24:00Z", "approve", null, null))))),
                svc.loadSection("sdlcChain", () -> new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("code", "MR-1210", "fix: close WebSocket connections in retry loop", "/code")))),
                new SectionResultDto<>(null, null)
        );
    }

    // ── Incident list data ──

    private List<IncidentListItemDto> buildIncidentList() {
        return List.of(
                new IncidentListItemDto("INC-0422", "API Gateway Latency Spike (>500ms)", "P1", "PENDING_APPROVAL", "AI", "Approval", "2026-04-16T09:40:00Z", "PT1H23M"),
                new IncidentListItemDto("INC-0421", "Database Connection Pool Exhaustion", "P2", "EXECUTING", "AI", "Approval", "2026-04-16T08:15:00Z", "PT2H48M"),
                new IncidentListItemDto("INC-0420", "Cache Miss Rate Spike in Product Service", "P3", "RESOLVED", "Hybrid", "Manual", "2026-04-15T14:30:00Z", "PT4H12M"),
                new IncidentListItemDto("INC-0419", "Certificate Expiry Warning (7 days)", "P3", "CLOSED", "AI", "Auto", "2026-04-14T10:00:00Z", "PT0H45M"),
                new IncidentListItemDto("INC-0418", "Memory Leak in Notification Worker", "P2", "LEARNING", "AI", "Approval", "2026-04-13T16:20:00Z", "PT6H30M")
        );
    }
}
