package com.sdlctower.domain.projectspace;

import com.sdlctower.domain.projectspace.dto.ProjectSpaceDtos.MemberRefDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProjectSpaceSeedCatalog {

    private static final Map<String, String> MEMBER_DIRECTORY = Map.ofEntries(
            Map.entry("u-003", "Ada Lovelace"),
            Map.entry("u-007", "Grace Hopper"),
            Map.entry("u-011", "Alan Turing"),
            Map.entry("u-012", "Katherine Johnson"),
            Map.entry("u-015", "Barbara Liskov"),
            Map.entry("u-020", "Margaret Hamilton"),
            Map.entry("u-021", "Carol Shaw"),
            Map.entry("u-030", "Linus Torvalds"),
            Map.entry("u-044", "Hiro Tanaka")
    );

    private static final ProjectSeed PROJ_42 = new ProjectSeed(
            "proj-42",
            "ws-default-001",
            "Gateway Migration",
            "DELIVERY",
            "u-007",
            "u-011",
            12,
            18,
            7,
            42,
            3,
            7,
            1,
            3,
            Instant.parse("2026-04-17T10:05:00Z"),
            false,
            defaultRoles(false, true, "UPCOMING")
    );

    private static final ProjectSeed PROJ_11 = new ProjectSeed(
            "proj-11",
            "ws-default-001",
            "Card Issuance",
            "DELIVERY",
            "u-007",
            "u-011",
            9,
            12,
            4,
            24,
            3,
            4,
            0,
            1,
            Instant.parse("2026-04-17T09:00:00Z"),
            false,
            defaultRoles(true, true, "UPCOMING")
    );

    private static final ProjectSeed PROJ_55 = new ProjectSeed(
            "proj-55",
            "ws-default-001",
            "Fraud Detection Expansion",
            "DISCOVERY",
            "u-007",
            "u-011",
            8,
            11,
            3,
            18,
            2,
            3,
            0,
            2,
            Instant.parse("2026-04-17T09:15:00Z"),
            false,
            defaultRoles(false, true, "UPCOMING")
    );

    private static final ProjectSeed PROJ_88 = new ProjectSeed(
            "proj-88",
            "ws-default-001",
            "Legacy Queue Decommission",
            "RETIRING",
            "u-007",
            "u-011",
            5,
            7,
            2,
            12,
            3,
            2,
            2,
            2,
            Instant.parse("2026-04-17T10:05:00Z"),
            false,
            defaultRoles(false, false, "ON")
    );

    private static final ProjectSeed PROJ_07 = new ProjectSeed(
            "proj-07",
            "ws-default-001",
            "Q1 Cost Reporting",
            "STEADY_STATE",
            "u-007",
            "u-011",
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            Instant.parse("2026-04-16T10:05:00Z"),
            false,
            defaultRoles(false, true, "UPCOMING")
    );

    private static final ProjectSeed PROJ_PRIVATE = new ProjectSeed(
            "proj-private-01",
            "ws-private-001",
            "Restricted Treasury Controls",
            "DELIVERY",
            "u-007",
            "u-011",
            4,
            5,
            2,
            8,
            2,
            2,
            0,
            1,
            Instant.parse("2026-04-17T08:00:00Z"),
            false,
            defaultRoles(false, true, "UPCOMING")
    );

    private static final ProjectSeed PROJ_DEGRADED = new ProjectSeed(
            "proj-degraded-001",
            "ws-default-001",
            "Degraded Project Feed",
            "DELIVERY",
            "u-007",
            "u-011",
            6,
            9,
            2,
            14,
            2,
            2,
            0,
            1,
            Instant.parse("2026-04-17T08:30:00Z"),
            true,
            defaultRoles(false, true, "UPCOMING")
    );

    private static final Map<String, ProjectSeed> PROJECTS = new LinkedHashMap<>();

    static {
        PROJECTS.put(PROJ_11.id(), PROJ_11);
        PROJECTS.put(PROJ_42.id(), PROJ_42);
        PROJECTS.put(PROJ_55.id(), PROJ_55);
        PROJECTS.put(PROJ_88.id(), PROJ_88);
        PROJECTS.put(PROJ_07.id(), PROJ_07);
        PROJECTS.put(PROJ_PRIVATE.id(), PROJ_PRIVATE);
        PROJECTS.put(PROJ_DEGRADED.id(), PROJ_DEGRADED);
    }

    public boolean exists(String projectId) {
        return PROJECTS.containsKey(projectId);
    }

    public ProjectSeed project(String projectId) {
        ProjectSeed seed = PROJECTS.get(projectId);
        if (seed == null) {
            throw new ResourceNotFoundException("Project " + projectId + " not found");
        }
        return seed;
    }

    public MemberRefDto memberRef(String memberId) {
        return new MemberRefDto(memberId, memberDisplayName(memberId));
    }

    public MemberRefDto nullableMemberRef(String memberId) {
        return memberId == null ? null : memberRef(memberId);
    }

    public String memberDisplayName(String memberId) {
        return MEMBER_DIRECTORY.getOrDefault(memberId, memberId);
    }

    public MemberRefDto riskOwner(String projectId, String category) {
        ProjectSeed project = project(projectId);
        return switch (category) {
            case "GOVERNANCE" -> memberRef(project.pmMemberId());
            default -> memberRef(project.techLeadMemberId());
        };
    }

    private static List<RoleSeed> defaultRoles(boolean aiOwnerAssigned, boolean sreBackupPresent, String sreOncallStatus) {
        return List.of(
                new RoleSeed("PM", "u-007", "OFF", "u-012"),
                new RoleSeed("ARCHITECT", "u-003", "OFF", null),
                new RoleSeed("TECH_LEAD", "u-011", "ON", "u-015"),
                new RoleSeed("QA_LEAD", "u-020", "OFF", "u-021"),
                new RoleSeed("SRE", "u-030", sreOncallStatus, sreBackupPresent ? "u-044" : null),
                new RoleSeed("AI_ADOPTION", aiOwnerAssigned ? "u-044" : null, aiOwnerAssigned ? "OFF" : "NONE", null)
        );
    }

    public record RoleSeed(
            String role,
            String memberId,
            String oncallStatus,
            String backupMemberId
    ) {}

    public record ProjectSeed(
            String id,
            String workspaceId,
            String name,
            String lifecycleStage,
            String pmMemberId,
            String techLeadMemberId,
            int requirementCount,
            int storyCount,
            int specCount,
            int taskCount,
            int deployCount,
            int activeSpecCount,
            int openIncidentCount,
            int pendingApprovals,
            Instant lastUpdatedAt,
            boolean environmentProjectionFails,
            List<RoleSeed> roles
    ) {}
}
