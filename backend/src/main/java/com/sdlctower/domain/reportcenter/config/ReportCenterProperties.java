package com.sdlctower.domain.reportcenter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reports")
public record ReportCenterProperties(
        String artifactDir,
        String signingSecret,
        String orgId
) {

    public ReportCenterProperties {
        artifactDir = artifactDir == null || artifactDir.isBlank() ? System.getProperty("java.io.tmpdir") + "/sdlc-report-artifacts" : artifactDir;
        signingSecret = signingSecret == null || signingSecret.isBlank() ? "report-center-local-secret" : signingSecret;
        orgId = orgId == null || orgId.isBlank() ? "org-default-001" : orgId;
    }
}
