package com.sdlctower.platform.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String sessionSecret = "local-dev-session-secret";
    private long sessionTtlSeconds = 28_800;
    private boolean teamBookEnabled = false;
    private int failedLoginLimit = 5;
    private long failedLoginWindowSeconds = 300;
    private boolean demoMode = false;
    private boolean allowAnonymousWorkspaceAccess = false;

    public String getSessionSecret() {
        return sessionSecret;
    }

    public void setSessionSecret(String sessionSecret) {
        this.sessionSecret = sessionSecret;
    }

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    public void setSessionTtlSeconds(long sessionTtlSeconds) {
        this.sessionTtlSeconds = sessionTtlSeconds;
    }

    public boolean isTeamBookEnabled() {
        return teamBookEnabled;
    }

    public void setTeamBookEnabled(boolean teamBookEnabled) {
        this.teamBookEnabled = teamBookEnabled;
    }

    public int getFailedLoginLimit() {
        return failedLoginLimit;
    }

    public void setFailedLoginLimit(int failedLoginLimit) {
        this.failedLoginLimit = failedLoginLimit;
    }

    public long getFailedLoginWindowSeconds() {
        return failedLoginWindowSeconds;
    }

    public void setFailedLoginWindowSeconds(long failedLoginWindowSeconds) {
        this.failedLoginWindowSeconds = failedLoginWindowSeconds;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    public boolean isAllowAnonymousWorkspaceAccess() {
        return allowAnonymousWorkspaceAccess;
    }

    public void setAllowAnonymousWorkspaceAccess(boolean allowAnonymousWorkspaceAccess) {
        this.allowAnonymousWorkspaceAccess = allowAnonymousWorkspaceAccess;
    }
}
