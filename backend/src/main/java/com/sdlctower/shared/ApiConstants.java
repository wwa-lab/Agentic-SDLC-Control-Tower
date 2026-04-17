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

    public static final String PIPELINE_PROFILES = API_V1 + "/pipeline-profiles";
    public static final String PIPELINE_PROFILES_ACTIVE = PIPELINE_PROFILES + "/active";
    public static final String TEAM_SPACE = API_V1 + "/team-space";
    public static final String PROJECT_SPACE = API_V1 + "/project-space";
    public static final String PROJECT_MANAGEMENT = API_V1 + "/project-management";
    public static final String DESIGN_MANAGEMENT = API_V1 + "/design-management";
}
