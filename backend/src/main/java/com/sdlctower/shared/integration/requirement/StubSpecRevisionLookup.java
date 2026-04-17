package com.sdlctower.shared.integration.requirement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StubSpecRevisionLookup implements SpecRevisionLookup {

    private static final Map<String, SpecRevisionInfo> SPECS = Map.ofEntries(
            Map.entry("SPEC-001", new SpecRevisionInfo(
                    "SPEC-001",
                    "SSO Authentication Flow Specification",
                    9,
                    "APPROVED",
                    "REQ-0001",
                    "proj-42",
                    "ws-default-001"
            )),
            Map.entry("SPEC-002", new SpecRevisionInfo(
                    "SPEC-002",
                    "IdP Configuration Management Spec",
                    2,
                    "DRAFT",
                    "REQ-0001",
                    "proj-42",
                    "ws-default-001"
            )),
            Map.entry("SPEC-010", new SpecRevisionInfo(
                    "SPEC-010",
                    "RBAC Permission Model Specification",
                    5,
                    "APPROVED",
                    "REQ-0002",
                    "proj-11",
                    "ws-default-001"
            )),
            Map.entry("SPEC-030", new SpecRevisionInfo(
                    "SPEC-030",
                    "Oracle 23ai Migration Specification",
                    7,
                    "REVIEW",
                    "REQ-0004",
                    "proj-55",
                    "ws-default-001"
            )),
            Map.entry("SPEC-040", new SpecRevisionInfo(
                    "SPEC-040",
                    "Audit Event Schema Specification",
                    3,
                    "APPROVED",
                    "REQ-0005",
                    "proj-42",
                    "ws-default-001"
            )),
            Map.entry("SPEC-041", new SpecRevisionInfo(
                    "SPEC-041",
                    "Audit Export API Specification",
                    4,
                    "DRAFT",
                    "REQ-0005",
                    "proj-42",
                    "ws-default-001"
            )),
            Map.entry("SPEC-050", new SpecRevisionInfo(
                    "SPEC-050",
                    "User Profile API Specification",
                    1,
                    "IMPLEMENTED",
                    "REQ-0006",
                    "proj-07",
                    "ws-default-001"
            )),
            Map.entry("SPEC-060", new SpecRevisionInfo(
                    "SPEC-060",
                    "SLA Monitoring and Reporting Spec",
                    3,
                    "APPROVED",
                    "REQ-0007",
                    "proj-88",
                    "ws-default-001"
            )),
            Map.entry("SPEC-080", new SpecRevisionInfo(
                    "SPEC-080",
                    "Report Export API Specification",
                    2,
                    "IMPLEMENTED",
                    "REQ-0009",
                    "proj-11",
                    "ws-default-001"
            )),
            Map.entry("SPEC-091", new SpecRevisionInfo(
                    "SPEC-091",
                    "AI Requirement Analysis Experience",
                    1,
                    "ARCHIVED",
                    "REQ-0010",
                    "proj-55",
                    "ws-default-001"
            )),
            Map.entry("SPEC-PRIVATE-01", new SpecRevisionInfo(
                    "SPEC-PRIVATE-01",
                    "Restricted Treasury Control Surface",
                    6,
                    "APPROVED",
                    "REQ-0100",
                    "proj-private-01",
                    "ws-private-001"
            ))
    );

    @Override
    public Map<String, SpecRevisionInfo> findByIds(Collection<String> specIds) {
        return specIds.stream()
                .distinct()
                .filter(SPECS::containsKey)
                .collect(Collectors.toMap(
                        specId -> specId,
                        SPECS::get,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<SpecRevisionInfo> listByWorkspace(String workspaceId) {
        return SPECS.values().stream()
                .filter(spec -> spec.workspaceId().equals(workspaceId))
                .sorted((left, right) -> left.specId().compareToIgnoreCase(right.specId()))
                .toList();
    }
}
