package com.sdlctower.shared;

/**
 * Centralized API path and version constants.
 * All controllers and tests reference these instead of magic strings.
 */
public final class ApiConstants {

    private ApiConstants() {}

    public static final String API_V1 = "/api/v1";

    /** Workspace-scoped URL prefix. All domain controllers use this. */
    public static final String WORKSPACES = API_V1 + "/workspaces";
    public static final String WORKSPACE_SCOPED = WORKSPACES + "/{workspaceId}";

    /** Auth workspace endpoints (allowlisted — no workspace prefix). */
    public static final String AUTH_WORKSPACE_SWITCH = API_V1 + "/auth/workspace";
    public static final String AUTH_WORKSPACES_LIST = API_V1 + "/auth/workspaces";
    public static final String AUTH_WORKSPACES_BY_KEY = API_V1 + "/auth/workspaces/by-key/{key}";

    public static final String WORKSPACE_CONTEXT = WORKSPACE_SCOPED + "/context";
    /** Legacy single-row workspace context endpoint (demo/guest path only). */
    public static final String WORKSPACE_CONTEXT_LEGACY = API_V1 + "/workspace-context";
    public static final String NAV_ENTRIES = API_V1 + "/nav/entries";
    public static final String AUTH = API_V1 + "/auth";
    public static final String AUTH_PROVIDERS = AUTH + "/providers";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_GUEST = AUTH + "/guest";
    public static final String AUTH_ME = AUTH + "/me";
    public static final String AUTH_LOGOUT = AUTH + "/logout";
    public static final String AUTH_TEAMBOOK_START = AUTH + "/sso/teambook/start";
    public static final String AUTH_TEAMBOOK_CALLBACK = AUTH + "/sso/teambook/callback";
    public static final String SHELL_HELP_LINKS = API_V1 + "/shell/help-links";
    public static final String SUPPORT_CONTACT = API_V1 + "/support/contact";
    public static final String DASHBOARD = WORKSPACE_SCOPED + "/dashboard";
    public static final String DASHBOARD_SUMMARY = DASHBOARD + "/summary";

    public static final String INCIDENTS = WORKSPACE_SCOPED + "/incidents";
    public static final String INCIDENT_DETAIL = INCIDENTS + "/{incidentId}";
    public static final String INCIDENT_ACTION_APPROVE = INCIDENT_DETAIL + "/actions/{actionId}/approve";
    public static final String INCIDENT_ACTION_REJECT = INCIDENT_DETAIL + "/actions/{actionId}/reject";

    public static final String REQUIREMENTS = WORKSPACE_SCOPED + "/requirements";
    public static final String REQUIREMENT_DETAIL = REQUIREMENTS + "/{requirementId}";
    public static final String REQUIREMENT_CHAIN = REQUIREMENT_DETAIL + "/chain";
    public static final String REQUIREMENT_ANALYSIS = REQUIREMENT_DETAIL + "/analysis";
    public static final String REQUIREMENT_GENERATE_STORIES = REQUIREMENT_DETAIL + "/generate-stories";
    public static final String REQUIREMENT_GENERATE_SPEC = REQUIREMENT_DETAIL + "/generate-spec";
    public static final String REQUIREMENT_STORY_GENERATE_SPEC = REQUIREMENTS + "/stories/{storyId}/generate-spec";
    public static final String REQUIREMENT_ANALYZE = REQUIREMENT_DETAIL + "/analyze";
    public static final String REQUIREMENT_INVOKE_SKILL = REQUIREMENT_DETAIL + "/invoke-skill";
    public static final String REQUIREMENT_NORMALIZE = REQUIREMENTS + "/normalize";
    public static final String REQUIREMENT_IMPORTS = REQUIREMENTS + "/imports";
    public static final String REQUIREMENT_IMPORT_DETAIL = REQUIREMENT_IMPORTS + "/{importId}";
    public static final String REQUIREMENT_SOURCES = REQUIREMENT_DETAIL + "/sources";
    public static final String REQUIREMENT_SOURCE_REFRESH = REQUIREMENTS + "/sources/{sourceId}/refresh";
    public static final String REQUIREMENT_SDD_DOCUMENTS = REQUIREMENT_DETAIL + "/sdd-documents";
    public static final String REQUIREMENT_SDD_DOCUMENTS_REFRESH = REQUIREMENT_SDD_DOCUMENTS + "/refresh";
    public static final String REQUIREMENT_SDD_DOCUMENT_DETAIL = REQUIREMENTS + "/documents/{documentId}";
    public static final String REQUIREMENT_DOCUMENT_REVIEWS = REQUIREMENT_SDD_DOCUMENT_DETAIL + "/reviews";
    public static final String REQUIREMENT_DOCUMENT_QUALITY_GATE = REQUIREMENT_SDD_DOCUMENT_DETAIL + "/quality-gate";
    public static final String REQUIREMENT_DOCUMENT_QUALITY_GATE_RUNS = REQUIREMENT_SDD_DOCUMENT_DETAIL + "/quality-gate-runs";
    public static final String REQUIREMENT_QUALITY_GATE_RUNS = REQUIREMENT_DETAIL + "/quality-gate-runs";
    public static final String REQUIREMENT_REVIEWS = REQUIREMENT_DETAIL + "/reviews";
    public static final String REQUIREMENT_AGENT_RUNS = REQUIREMENT_DETAIL + "/agent-runs";
    public static final String REQUIREMENT_AGENT_RUN_DETAIL = REQUIREMENTS + "/agent-runs/{executionId}";
    public static final String REQUIREMENT_AGENT_RUN_CALLBACK = REQUIREMENT_AGENT_RUN_DETAIL + "/callback";
    public static final String REQUIREMENT_AGENT_RUN_STAGE_EVENTS = REQUIREMENT_AGENT_RUN_DETAIL + "/stage-events";
    public static final String REQUIREMENT_AGENT_RUN_MERGE_CONFIRMATION = REQUIREMENT_AGENT_RUN_DETAIL + "/merge-confirmation";
    public static final String REQUIREMENT_TRACEABILITY = REQUIREMENT_DETAIL + "/traceability";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH = REQUIREMENTS + "/knowledge-graph";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH_NODE = REQUIREMENT_KNOWLEDGE_GRAPH + "/nodes/{nodeId}";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH_IMPACT = REQUIREMENT_KNOWLEDGE_GRAPH + "/impact";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH_HEALTH = REQUIREMENT_KNOWLEDGE_GRAPH + "/health";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH_IMPORT = REQUIREMENT_KNOWLEDGE_GRAPH + "/import";
    public static final String REQUIREMENT_KNOWLEDGE_GRAPH_SYNC = REQUIREMENT_KNOWLEDGE_GRAPH + "/sync";

    public static final String PIPELINE_PROFILES = API_V1 + "/pipeline-profiles";
    public static final String PIPELINE_PROFILES_ACTIVE = PIPELINE_PROFILES + "/active";
    public static final String TEAM_SPACE = WORKSPACE_SCOPED + "/team-space";
    public static final String PROJECT_SPACE = WORKSPACE_SCOPED + "/project-space";
    public static final String PROJECT_MANAGEMENT = WORKSPACE_SCOPED + "/project-management";
    public static final String DESIGN_MANAGEMENT = WORKSPACE_SCOPED + "/design-management";
    public static final String TESTING = WORKSPACE_SCOPED + "/testing";
    public static final String TESTING_MANAGEMENT = WORKSPACE_SCOPED + "/testing-management";
    public static final String REPORTS_BASE = WORKSPACE_SCOPED + "/reports";
    public static final String CODE_BUILD_MANAGEMENT = WORKSPACE_SCOPED + "/code-build-management";

    public static final String DEPLOYMENT_MANAGEMENT = WORKSPACE_SCOPED + "/deployment-management";
    public static final String DEPLOYMENT_CATALOG = DEPLOYMENT_MANAGEMENT + "/catalog";
    public static final String DEPLOYMENT_APPLICATIONS = DEPLOYMENT_MANAGEMENT + "/applications";
    public static final String DEPLOYMENT_APPLICATION_DETAIL = DEPLOYMENT_APPLICATIONS + "/{applicationId}";
    public static final String DEPLOYMENT_APP_ENVIRONMENTS = DEPLOYMENT_APPLICATION_DETAIL + "/environments/{environmentName}";
    public static final String DEPLOYMENT_RELEASES = DEPLOYMENT_MANAGEMENT + "/releases";
    public static final String DEPLOYMENT_RELEASE_DETAIL = DEPLOYMENT_RELEASES + "/{releaseId}";
    public static final String DEPLOYMENT_RELEASE_AI_NOTES = DEPLOYMENT_RELEASE_DETAIL + "/ai-notes/regenerate";
    public static final String DEPLOYMENT_DEPLOYS = DEPLOYMENT_MANAGEMENT + "/deploys";
    public static final String DEPLOYMENT_DEPLOY_DETAIL = DEPLOYMENT_DEPLOYS + "/{deployId}";
    public static final String DEPLOYMENT_DEPLOY_AI_SUMMARY = DEPLOYMENT_DEPLOY_DETAIL + "/ai-summary/regenerate";
    public static final String DEPLOYMENT_TRACEABILITY = DEPLOYMENT_MANAGEMENT + "/traceability";
    public static final String DEPLOYMENT_WORKSPACE_AI_SUMMARY = DEPLOYMENT_MANAGEMENT + "/ai-summary/regenerate";

    /** Webhook endpoints — NOT workspace-scoped; workspace resolved from payload. */
    public static final String WEBHOOKS_BASE = API_V1 + "/integration/webhooks";
    public static final String WEBHOOKS_JENKINS = WEBHOOKS_BASE + "/jenkins";
    public static final String WEBHOOKS_GITHUB = WEBHOOKS_BASE + "/github";

    /** Cross-workspace fleet reports — AUDITOR / PLATFORM_ADMIN only. */
    public static final String REPORTS_FLEET = API_V1 + "/reports/fleet";

    public static final String AI_CENTER = WORKSPACE_SCOPED + "/ai-center";
    public static final String AI_CENTER_METRICS = AI_CENTER + "/metrics";
    public static final String AI_CENTER_STAGE_COVERAGE = AI_CENTER + "/stage-coverage";
    public static final String AI_CENTER_SKILLS = AI_CENTER + "/skills";
    public static final String AI_CENTER_SKILL_DETAIL = AI_CENTER + "/skills/{skillKey}";
    public static final String AI_CENTER_RUNS = AI_CENTER + "/runs";
    public static final String AI_CENTER_RUN_DETAIL = AI_CENTER + "/runs/{executionId}";
}
