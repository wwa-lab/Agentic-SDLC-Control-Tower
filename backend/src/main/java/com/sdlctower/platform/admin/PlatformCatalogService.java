package com.sdlctower.platform.admin;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PlatformCatalogService {

    private final Instant now = Instant.parse("2026-05-02T00:00:00Z");

    public List<Map<String, Object>> applications() {
        return List.of(row(
                "id", "app-payment-gateway-pro",
                "key", "payment-gateway-pro",
                "name", "Payment-Gateway-Pro",
                "ownerSnowGroupId", "snow-fin-tech-ops",
                "criticality", "tier-1",
                "status", "active"
        ));
    }

    public List<Map<String, Object>> snowGroups() {
        return List.of(row(
                "id", "snow-fin-tech-ops",
                "servicenowGroupName", "FIN-TECH-OPS",
                "displayName", "FIN-TECH-OPS",
                "ownerEmail", "fin-tech-ops@example.com",
                "escalationPolicy", "business-hours-primary",
                "status", "active"
        ));
    }

    public List<Map<String, Object>> workspaces() {
        return List.of(row(
                "id", "ws-default-001",
                "key", "global-sdlc-tower",
                "name", "Global SDLC Tower",
                "applicationId", "app-payment-gateway-pro",
                "snowGroupId", "snow-fin-tech-ops",
                "status", "active"
        ));
    }

    public Map<String, Object> resolveScope(String workspaceId, String applicationId, String snowGroupId, String projectId) {
        String resolvedApplication = blankToNull(applicationId) != null ? applicationId : "app-payment-gateway-pro";
        String resolvedSnow = blankToNull(snowGroupId) != null ? snowGroupId : "snow-fin-tech-ops";
        String resolvedWorkspace = blankToNull(workspaceId) != null ? workspaceId : "ws-default-001";
        String resolvedProject = blankToNull(projectId);
        return row(
                "inputType", resolvedProject != null ? "project" : resolvedWorkspace != null ? "workspace" : "application",
                "inputId", resolvedProject != null ? resolvedProject : resolvedWorkspace,
                "applicationId", resolvedApplication,
                "snowGroupId", resolvedSnow,
                "workspaceId", resolvedWorkspace,
                "projectId", resolvedProject,
                "scopeChain", List.of(
                        row("scopeType", "platform", "scopeId", "*"),
                        row("scopeType", "application", "scopeId", resolvedApplication),
                        row("scopeType", "snow_group", "scopeId", resolvedSnow),
                        row("scopeType", "workspace", "scopeId", resolvedWorkspace)
                )
        );
    }

    public List<Map<String, Object>> templates() {
        return List.of(
                template("tpl-page-control", "control-tower-page", "Control Tower Page", "page", "published", 3),
                template("tpl-flow-approval", "approval-flow", "Approval Flow", "flow", "published", 2),
                template("tpl-policy-release", "release-policy", "Release Policy", "policy", "draft", 1),
                template("tpl-ai-summary", "ai-summary", "AI Summary Prompt", "ai", "deprecated", 5)
        );
    }

    public Map<String, Object> templateDetail(String id) {
        Map<String, Object> template = templates().stream()
                .filter(item -> id.equals(item.get("id")))
                .findFirst()
                .orElse(templates().get(0));
        return row(
                "template", enrich(template, "description", "Reusable platform template for " + template.get("name")),
                "version", row("id", id + "-v" + template.get("version"), "templateId", id, "version", template.get("version"),
                        "body", row("layout", "catalog-detail", "density", "high"),
                        "createdAt", now.toString(), "createdBy", "43910516"),
                "inheritance", row("layout", row(
                        "effectiveValue", "catalog-detail",
                        "winningLayer", "platform",
                        "layers", row("platform", "catalog-detail", "application", null, "snowGroup", null, "workspace", null, "project", null)
                ))
        );
    }

    public List<Map<String, Object>> templateVersions(String id) {
        return List.of(
                row("id", id + "-v1", "templateId", id, "version", 1, "body", row("density", "normal"), "createdAt", now.minusSeconds(86_400).toString(), "createdBy", "43910516"),
                row("id", id + "-v2", "templateId", id, "version", 2, "body", row("density", "high"), "createdAt", now.toString(), "createdBy", "43910516")
        );
    }

    public List<Map<String, Object>> configurations() {
        return List.of(
                config("cfg-nav-density", "shell.nav.density", "component", "platform", "*", false),
                config("cfg-ai-default", "ai.autonomy.default", "ai-config", "application", "app-payment-gateway-pro", true),
                config("cfg-snow-notify", "notification.snow.escalation", "notification", "snow_group", "snow-fin-tech-ops", true),
                config("cfg-project-view", "project.view.default", "view-rule", "workspace", "ws-default-001", false)
        );
    }

    public Map<String, Object> configurationDetail(String id) {
        Map<String, Object> summary = configurations().stream()
                .filter(item -> id.equals(item.get("id")))
                .findFirst()
                .orElse(configurations().get(0));
        return enrich(summary,
                "body", row("enabled", true, "source", summary.get("scopeType")),
                "platformDefaultBody", row("enabled", true, "source", "platform"),
                "driftFields", Boolean.TRUE.equals(summary.get("hasDrift")) ? List.of("source") : List.of());
    }

    public List<Map<String, Object>> audit() {
        return List.of(
                audit("aud-0001", "system", "permission_change", "role.grant", "role_assignment", "ra-admin-platform", "platform", "*", "success"),
                audit("aud-0002", "43910516", "config_change", "configuration.update", "configuration", "cfg-ai-default", "application", "app-payment-gateway-pro", "success"),
                audit("aud-0003", "43910516", "integration.test", "connection.test", "connection", "conn-jira-ws1", "workspace", "ws-default-001", "failure")
        );
    }

    public List<Map<String, Object>> policies() {
        return List.of(
                policy("pol-release-approval", "release-approval", "Release Approval", "approval", "active", "deploy.release"),
                policy("pol-ai-autonomy", "ai-autonomy", "AI Autonomy", "autonomy", "active", "skill.execution"),
                policy("pol-risk-threshold", "risk-threshold", "Risk Threshold", "risk-threshold", "draft", null)
        );
    }

    public List<Map<String, Object>> policyExceptions(String policyId) {
        return List.of(row(
                "id", "ex-" + policyId,
                "policyId", policyId,
                "reason", "Temporary migration exception",
                "requesterId", "43910000",
                "approverId", "43910516",
                "createdAt", now.toString(),
                "expiresAt", now.plusSeconds(604_800).toString(),
                "revokedAt", null
        ));
    }

    public List<Map<String, Object>> adapters() {
        return List.of(
                row("kind", "jira", "label", "Jira", "supportedModes", List.of("pull", "push", "both"), "capabilities", List.of("requirement-source", "support-story")),
                row("kind", "confluence", "label", "Confluence", "supportedModes", List.of("pull"), "capabilities", List.of("guideline", "business-context")),
                row("kind", "jenkins", "label", "Jenkins", "supportedModes", List.of("push"), "capabilities", List.of("build", "deploy")),
                row("kind", "servicenow", "label", "ServiceNow", "supportedModes", List.of("pull", "push", "both"), "capabilities", List.of("incident", "snow-group"))
        );
    }

    public List<Map<String, Object>> connections() {
        return List.of(
                connection("conn-jira-ws1", "jira", "enabled", true),
                connection("conn-confluence-ws1", "confluence", "enabled", true),
                connection("conn-jenkins-ws1", "jenkins", "error", false),
                connection("conn-servicenow-ws1", "servicenow", "disabled", null)
        );
    }

    public Map<String, Object> testConnection(String id) {
        boolean ok = !"conn-jenkins-ws1".equals(id);
        return row("ok", ok, "latencyMs", ok ? 42 : 850, "message", ok ? "Connection probe succeeded" : "Connection probe failed in local stub");
    }

    private Map<String, Object> template(String id, String key, String name, String kind, String status, int version) {
        return row("id", id, "key", key, "name", name, "kind", kind, "status", status, "version", version,
                "ownerId", "43910516", "lastModifiedAt", now.toString());
    }

    private Map<String, Object> config(String id, String key, String kind, String scopeType, String scopeId, boolean drift) {
        return row("id", id, "key", key, "kind", kind, "scopeType", scopeType, "scopeId", scopeId,
                "parentId", null, "status", "active", "hasDrift", drift, "lastModifiedAt", now.toString());
    }

    private Map<String, Object> audit(String id, String actor, String category, String action, String objectType, String objectId, String scopeType, String scopeId, String outcome) {
        return row("id", id, "timestamp", now.toString(), "actor", actor, "actorType", "system".equals(actor) ? "system" : "user",
                "category", category, "action", action, "objectType", objectType, "objectId", objectId,
                "scopeType", scopeType, "scopeId", scopeId, "outcome", outcome, "evidenceRef", null, "payload", row("local", true));
    }

    private Map<String, Object> policy(String id, String key, String name, String category, String status, String boundTo) {
        return row("id", id, "key", key, "name", name, "category", category, "scopeType", "platform", "scopeId", "*",
                "boundTo", boundTo, "version", 1, "status", status, "body", row("enabled", true),
                "createdAt", now.toString(), "createdBy", "43910516");
    }

    private Map<String, Object> connection(String id, String kind, String status, Boolean lastTestOk) {
        return row("id", id, "kind", kind, "scopeWorkspaceId", "ws-default-001",
                "applicationId", "app-payment-gateway-pro", "applicationName", "Payment-Gateway-Pro",
                "snowGroupId", "snow-fin-tech-ops", "snowGroupName", "FIN-TECH-OPS",
                "baseUrl", "https://" + kind + ".company.com", "credentialRef", "cred-" + kind + "-demo",
                "syncMode", "jira".equals(kind) || "servicenow".equals(kind) ? "both" : "pull",
                "pullSchedule", "0 */15 * * * *", "pushUrl", null, "status", status,
                "lastSyncAt", now.toString(), "lastTestAt", now.toString(), "lastTestOk", lastTestOk);
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private Map<String, Object> enrich(Map<String, Object> source, Object... values) {
        Map<String, Object> map = new LinkedHashMap<>(source);
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
