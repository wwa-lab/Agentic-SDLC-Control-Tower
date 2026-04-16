package com.sdlctower.domain.teamspace;

import com.sdlctower.domain.teamspace.dto.AccountableOwnerDto;
import com.sdlctower.domain.teamspace.dto.CoverageGapDto;
import com.sdlctower.domain.teamspace.dto.ExceptionOverrideDto;
import com.sdlctower.domain.teamspace.dto.FieldDto;
import com.sdlctower.domain.teamspace.dto.LineageDto;
import com.sdlctower.domain.teamspace.dto.LineageHopDto;
import com.sdlctower.domain.teamspace.dto.LinkDto;
import com.sdlctower.domain.teamspace.dto.MemberMatrixDto;
import com.sdlctower.domain.teamspace.dto.MemberMatrixRowDto;
import com.sdlctower.domain.teamspace.dto.OncallOwnerDto;
import com.sdlctower.domain.teamspace.dto.ProjectCardDto;
import com.sdlctower.domain.teamspace.dto.ProjectDistributionDto;
import com.sdlctower.domain.teamspace.dto.TeamDefaultTemplatesDto;
import com.sdlctower.domain.teamspace.dto.TeamOperatingModelDto;
import com.sdlctower.domain.teamspace.dto.TemplateEntryDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TeamSpaceSeedCatalog {

    private static final WorkspaceSeed DEFAULT = new WorkspaceSeed(
            "ws-default-001",
            "Global SDLC Tower",
            "app-payment-gateway-pro",
            "Payment-Gateway-Pro",
            "snow-fin-tech-ops",
            "FIN-TECH-OPS",
            "u-007",
            "Grace Hopper",
            false,
            7,
            4,
            "GLOBAL-SDLC-TOWER"
    );

    private static final WorkspaceSeed LEGACY = new WorkspaceSeed(
            "ws-legacy-001",
            "Legacy Mainframe",
            "app-legacy-core",
            "Legacy Core",
            null,
            null,
            "u-099",
            "Katherine Johnson",
            true,
            2,
            1,
            "LEGACY-MAINFRAME"
    );

    private static final WorkspaceSeed DEGRADED = new WorkspaceSeed(
            "ws-degraded-001",
            "Global SDLC Tower",
            "app-payment-gateway-pro",
            "Payment-Gateway-Pro",
            "snow-fin-tech-ops",
            "FIN-TECH-OPS",
            "u-007",
            "Grace Hopper",
            false,
            7,
            4,
            "GLOBAL-SDLC-TOWER"
    );

    private static final WorkspaceSeed PRIVATE = new WorkspaceSeed(
            "ws-private-001",
            "Restricted Finance Workspace",
            "app-payment-gateway-pro",
            "Payment-Gateway-Pro",
            "snow-fin-tech-ops",
            "FIN-TECH-OPS",
            "u-007",
            "Grace Hopper",
            false,
            4,
            2,
            "GLOBAL-SDLC-TOWER"
    );

    private static final Map<String, WorkspaceSeed> WORKSPACES = Map.of(
            DEFAULT.id(), DEFAULT,
            LEGACY.id(), LEGACY,
            DEGRADED.id(), DEGRADED,
            PRIVATE.id(), PRIVATE
    );

    public boolean exists(String workspaceId) {
        return WORKSPACES.containsKey(workspaceId);
    }

    public WorkspaceSeed workspace(String workspaceId) {
        WorkspaceSeed workspace = WORKSPACES.get(workspaceId);
        if (workspace == null) {
            throw new ResourceNotFoundException("Workspace " + workspaceId + " not found");
        }
        return workspace;
    }

    public TeamOperatingModelDto operatingModel(String workspaceId) {
        WorkspaceSeed workspace = workspace(workspaceId);
        String operatingMode = workspace.compatibilityMode() ? "HIGH_GOVERNANCE" : "STANDARD";
        String approvalMode = workspace.compatibilityMode() ? "MULTI_APPROVER" : "REVIEWER_REQUIRED";

        return new TeamOperatingModelDto(
                new FieldDto<>(operatingMode, lineage(workspace.compatibilityMode() ? "WORKSPACE" : "APPLICATION", workspace.compatibilityMode())),
                new FieldDto<>(approvalMode, lineage("WORKSPACE", true)),
                new FieldDto<>("HUMAN_IN_LOOP", lineage("PLATFORM", false)),
                new FieldDto<>(
                        new OncallOwnerDto("u-011", "Alan Turing", "rot-fin-tech-primary"),
                        lineage("WORKSPACE", false)
                ),
                List.of(
                        new AccountableOwnerDto("DELIVERY", "u-007", "Grace Hopper"),
                        new AccountableOwnerDto("APPROVAL", "u-003", "Ada Lovelace"),
                        new AccountableOwnerDto("INCIDENT", "u-011", "Alan Turing"),
                        new AccountableOwnerDto("GOVERNANCE", "u-007", "Grace Hopper")
                ),
                new LinkDto("/platform?view=config&workspaceId=" + workspaceId + "&section=operating-model", false)
        );
    }

    public MemberMatrixDto members(String workspaceId) {
        List<CoverageGapDto> coverageGaps = workspaceId.equals(LEGACY.id())
                ? List.of(new CoverageGapDto(
                        "ONCALL_GAP",
                        "No primary oncall is configured for the next 24 hours",
                        "2026-04-18 00:00 – 2026-04-19 00:00"
                ))
                : List.of(new CoverageGapDto(
                        "BACKUP_MISSING",
                        "No secondary oncall assigned for the 2026-04-19 – 2026-04-21 window",
                        "2026-04-19 – 2026-04-21"
                ));

        return new MemberMatrixDto(
                List.of(
                        new MemberMatrixRowDto(
                                "u-007",
                                "Grace Hopper",
                                List.of("TEAM_LEAD", "APPROVER"),
                                "OFF",
                                List.of("APPROVE", "CONFIGURE"),
                                Instant.parse("2026-04-17T09:42:00Z")
                        ),
                        new MemberMatrixRowDto(
                                "u-011",
                                "Alan Turing",
                                List.of("SRE", "ONCALL_PRIMARY"),
                                "ON",
                                List.of("DEPLOY", "TRIAGE"),
                                Instant.parse("2026-04-17T10:05:00Z")
                        ),
                        new MemberMatrixRowDto(
                                "u-003",
                                "Ada Lovelace",
                                List.of("ARCHITECT", "APPROVER"),
                                "UPCOMING",
                                List.of("REVIEW", "APPROVE"),
                                Instant.parse("2026-04-16T19:10:00Z")
                        )
                ),
                coverageGaps,
                new LinkDto("/platform?view=access&workspaceId=" + workspaceId, false)
        );
    }

    public TeamDefaultTemplatesDto templates(String workspaceId) {
        Map<String, List<TemplateEntryDto>> groups = new LinkedHashMap<>();
        groups.put("PAGE", List.of(
                new TemplateEntryDto(
                        "tpl-page-team-space",
                        "Team Space Layout",
                        "1.0.0",
                        "PAGE",
                        new LineageDto("PLATFORM", false, List.of()),
                        null
                )
        ));
        groups.put("POLICY", List.of(
                new TemplateEntryDto(
                        "tpl-policy-approval",
                        "Default Approval Policy",
                        "2.1.0",
                        "POLICY",
                        new LineageDto("APPLICATION", false, List.of()),
                        null
                )
        ));
        groups.put("WORKFLOW", List.of());
        groups.put("SKILL_PACK", List.of(
                new TemplateEntryDto(
                        "tpl-skill-standard-sdd",
                        "Standard SDD Skill Pack",
                        "1.2.0",
                        "SKILL_PACK",
                        new LineageDto("PLATFORM", false, List.of()),
                        null
                )
        ));
        groups.put("AI_DEFAULT", List.of(
                new TemplateEntryDto(
                        "tpl-ai-default",
                        "AI Default Config",
                        "0.3.0",
                        "AI_DEFAULT",
                        new LineageDto("WORKSPACE", true, List.of()),
                        "/platform?view=config&workspaceId=" + workspaceId + "&section=ai"
                )
        ));

        return new TeamDefaultTemplatesDto(
                groups,
                List.of(
                        new ExceptionOverrideDto(
                                "tpl-policy-approval",
                                "Default Approval Policy",
                                "PROJECT",
                                "proj-42",
                                "Gateway Migration",
                                Instant.parse("2026-03-12T00:00:00Z"),
                                "u-007"
                        )
                )
        );
    }

    public ProjectDistributionDto projects(String workspaceId) {
        Map<String, List<ProjectCardDto>> groups = new LinkedHashMap<>();
        groups.put("HEALTHY", List.of(
                new ProjectCardDto(
                        "proj-11",
                        "Card Issuance",
                        "DELIVERY",
                        "HEALTHY",
                        null,
                        4,
                        0,
                        "/project-space/proj-11"
                )
        ));
        groups.put("AT_RISK", List.of(
                new ProjectCardDto(
                        "proj-42",
                        "Gateway Migration",
                        "DELIVERY",
                        "AT_RISK",
                        "2 blocked specs",
                        7,
                        1,
                        "/project-space/proj-42"
                ),
                new ProjectCardDto(
                        "proj-55",
                        "Fraud Detection Expansion",
                        "DISCOVERY",
                        "AT_RISK",
                        "Pending approval policy exception",
                        3,
                        0,
                        "/project-space/proj-55"
                )
        ));
        groups.put("CRITICAL", List.of(
                new ProjectCardDto(
                        "proj-88",
                        "Legacy Queue Decommission",
                        "RETIRING",
                        "CRITICAL",
                        "Incident hotspot and rollback instability",
                        2,
                        2,
                        "/project-space/proj-88"
                )
        ));
        groups.put("ARCHIVED", List.of(
                new ProjectCardDto(
                        "proj-07",
                        "Q1 Cost Reporting",
                        "STEADY_STATE",
                        "ARCHIVED",
                        null,
                        0,
                        0,
                        "/project-space/proj-07"
                )
        ));

        return new ProjectDistributionDto(
                groups,
                Map.of(
                        "HEALTHY", groups.get("HEALTHY").size(),
                        "AT_RISK", groups.get("AT_RISK").size(),
                        "CRITICAL", groups.get("CRITICAL").size(),
                        "ARCHIVED", groups.get("ARCHIVED").size()
                )
        );
    }

    private LineageDto lineage(String origin, boolean overridden) {
        return new LineageDto(
                origin,
                overridden,
                List.of(
                        new LineageHopDto("PLATFORM", "STANDARD", Instant.parse("2026-01-10T00:00:00Z"), "platform-admin"),
                        new LineageHopDto("APPLICATION", "STANDARD", Instant.parse("2026-02-01T00:00:00Z"), "app-owner-payments")
                )
        );
    }

    public record WorkspaceSeed(
            String id,
            String name,
            String applicationId,
            String applicationName,
            String snowGroupId,
            String snowGroupName,
            String ownerId,
            String ownerDisplayName,
            boolean compatibilityMode,
            int projectCount,
            int environmentCount,
            String requirementWorkspaceId
    ) {}
}
