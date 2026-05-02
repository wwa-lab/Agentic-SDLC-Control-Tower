package com.sdlctower.shared;

/**
 * Centralized API path and version constants.
 * All controllers and tests reference these instead of magic strings.
 */
public final class ApiConstants {

    private ApiConstants() {}

    public static final String API_V1 = "/api/v1";

    public static final String WORKSPACE_CONTEXT = API_V1 + "/workspace-context";
    public static final String NAV_ENTRIES = API_V1 + "/nav/entries";
    public static final String DASHBOARD = API_V1 + "/dashboard";
    public static final String DASHBOARD_SUMMARY = DASHBOARD + "/summary";

    public static final String INCIDENTS = API_V1 + "/incidents";
    public static final String INCIDENT_DETAIL = INCIDENTS + "/{incidentId}";
    public static final String INCIDENT_ACTION_APPROVE = INCIDENT_DETAIL + "/actions/{actionId}/approve";
    public static final String INCIDENT_ACTION_REJECT = INCIDENT_DETAIL + "/actions/{actionId}/reject";

    public static final String REQUIREMENTS = API_V1 + "/requirements";
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
    public static final String TEAM_SPACE = API_V1 + "/team-space";
    public static final String PROJECT_SPACE = API_V1 + "/project-space";
    public static final String PROJECT_MANAGEMENT = API_V1 + "/project-management";
    public static final String DESIGN_MANAGEMENT = API_V1 + "/design-management";
    public static final String TESTING = API_V1 + "/testing";
    public static final String TESTING_MANAGEMENT = API_V1 + "/testing-management";
    public static final String REPORTS_BASE = API_V1 + "/reports";
    public static final String CODE_BUILD_MANAGEMENT = API_V1 + "/code-build-management";

    public static final String DEPLOYMENT_MANAGEMENT = API_V1 + "/deployment-management";
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
    public static final String DEPLOYMENT_WORKSPACE_AI_SUMMARY = DEPLOYMENT_MANAGEMENT + "/workspaces/{workspaceId}/ai-summary/regenerate";
    public static final String DEPLOYMENT_WEBHOOKS_JENKINS = DEPLOYMENT_MANAGEMENT + "/webhooks/jenkins";

    public static final String AI_CENTER = API_V1 + "/ai-center";
    public static final String AI_CENTER_METRICS = AI_CENTER + "/metrics";
    public static final String AI_CENTER_STAGE_COVERAGE = AI_CENTER + "/stage-coverage";
    public static final String AI_CENTER_SKILLS = AI_CENTER + "/skills";
    public static final String AI_CENTER_SKILL_DETAIL = AI_CENTER + "/skills/{skillKey}";
    public static final String AI_CENTER_RUNS = AI_CENTER + "/runs";
    public static final String AI_CENTER_RUN_DETAIL = AI_CENTER + "/runs/{executionId}";
}
