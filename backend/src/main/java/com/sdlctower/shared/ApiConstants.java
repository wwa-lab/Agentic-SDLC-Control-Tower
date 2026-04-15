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
}
