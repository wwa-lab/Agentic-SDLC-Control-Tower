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
@ConditionalOnProperty(name = "app.requirement-control-plane.confluence.provider", havingValue = "real")
public class ConfluenceRequirementSourceProvider implements RequirementSourceProvider {
    private static final Pattern PAGE_ID = Pattern.compile("(?:pages/|pageId=)(\\d+)");

    private final RestClient restClient;

    public ConfluenceRequirementSourceProvider(
            RestClient.Builder builder,
            RequirementControlPlaneProperties properties
    ) {
        RequirementControlPlaneProperties.SourceIntegration confluence = properties.getConfluence();
        if (isBlank(confluence.getBaseUrl())) {
            throw new IllegalStateException("Confluence integration requires app.requirement-control-plane.confluence.base-url");
        }
        RestClient.Builder configured = builder.baseUrl(trimTrailingSlash(confluence.getBaseUrl()));
        String authHeader = authHeader(confluence);
        if (!isBlank(authHeader)) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        this.restClient = configured.build();
    }

    @Override
    public boolean supports(String sourceType) {
        return "CONFLUENCE".equalsIgnoreCase(sourceType);
    }

    @Override
    public SourceMetadata refresh(RequirementSourceReferenceEntity source) {
        String pageId = firstNonBlank(source.getExternalId(), inferPageId(source.getUrl()));
        if (isBlank(pageId)) {
            throw new IllegalArgumentException("Unable to infer Confluence page id from source " + source.getId());
        }

        ConfluencePageResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/content/{pageId}")
                        .queryParam("expand", "version")
                        .build(pageId))
                .retrieve()
                .body(ConfluencePageResponse.class);

        if (response == null) {
            throw new IllegalStateException("Confluence returned no page payload for " + pageId);
        }

        Instant updatedAt = response.version() == null ? Instant.now() : parseInstant(response.version().when());
        return new SourceMetadata(
                pageId,
                firstNonBlank(response.title(), source.getTitle()),
                updatedAt,
                Instant.now(),
                "FRESH",
                null
        );
    }

    private String inferPageId(String value) {
        if (value == null) return null;
        Matcher matcher = PAGE_ID.matcher(value);
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

    private record ConfluencePageResponse(String id, String title, ConfluenceVersion version) {}

    private record ConfluenceVersion(int number, String when) {}
}
