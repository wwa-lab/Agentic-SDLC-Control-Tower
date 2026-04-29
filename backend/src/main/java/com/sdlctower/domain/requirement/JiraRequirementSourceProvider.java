package com.sdlctower.domain.requirement;

import com.sdlctower.domain.requirement.persistence.RequirementSourceReferenceEntity;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.requirement-control-plane.jira.provider", havingValue = "real")
public class JiraRequirementSourceProvider implements RequirementSourceProvider {
    private static final Pattern ISSUE_KEY = Pattern.compile("([A-Z][A-Z0-9]+-\\d+)");

    private final RestClient restClient;

    public JiraRequirementSourceProvider(
            RestClient.Builder builder,
            RequirementControlPlaneProperties properties
    ) {
        RequirementControlPlaneProperties.SourceIntegration jira = properties.getJira();
        if (isBlank(jira.getBaseUrl())) {
            throw new IllegalStateException("Jira integration requires app.requirement-control-plane.jira.base-url");
        }
        RestClient.Builder configured = builder.baseUrl(trimTrailingSlash(jira.getBaseUrl()));
        String authHeader = authHeader(jira);
        if (!isBlank(authHeader)) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        this.restClient = configured.build();
    }

    @Override
    public boolean supports(String sourceType) {
        return "JIRA".equalsIgnoreCase(sourceType);
    }

    @Override
    public SourceMetadata refresh(RequirementSourceReferenceEntity source) {
        String issueKey = firstNonBlank(source.getExternalId(), inferIssueKey(source.getUrl()));
        if (isBlank(issueKey)) {
            throw new IllegalArgumentException("Unable to infer Jira issue key from source " + source.getId());
        }

        JiraIssueResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/issue/{issueKey}")
                        .queryParam("fields", "summary,updated,status")
                        .build(issueKey))
                .retrieve()
                .body(JiraIssueResponse.class);

        if (response == null || response.fields() == null) {
            throw new IllegalStateException("Jira returned no issue payload for " + issueKey);
        }

        String status = response.fields().status() == null ? null : response.fields().status().name();
        String freshness = "Done".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status) ? "SOURCE_CHANGED" : "FRESH";
        return new SourceMetadata(
                issueKey,
                firstNonBlank(response.fields().summary(), source.getTitle()),
                parseInstant(response.fields().updated()),
                Instant.now(),
                freshness,
                null
        );
    }

    private String inferIssueKey(String value) {
        if (value == null) return null;
        Matcher matcher = ISSUE_KEY.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String authHeader(RequirementControlPlaneProperties.SourceIntegration properties) {
        if (!isBlank(properties.getBearerToken())) {
            return "Bearer " + properties.getBearerToken();
        }
        if (!isBlank(properties.getEmail()) && !isBlank(properties.getApiToken())) {
            String token = properties.getEmail() + ":" + properties.getApiToken();
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    private static Instant parseInstant(String value) {
        if (isBlank(value)) return Instant.now();
        return Instant.parse(value.replaceFirst("([+-]\\d{2})(\\d{2})$", "$1:$2"));
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record JiraIssueResponse(String key, JiraFields fields) {}

    private record JiraFields(String summary, String updated, JiraStatus status) {}

    private record JiraStatus(String name) {}
}
