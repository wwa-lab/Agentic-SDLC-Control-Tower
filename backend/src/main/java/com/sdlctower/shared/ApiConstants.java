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
}
