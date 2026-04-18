package com.sdlctower.domain.deploymentmanagement.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.JenkinsEventType;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class JenkinsPayloadParser {

    private final ObjectMapper objectMapper;

    public JenkinsPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record ParsedEvent(
            JenkinsEventType eventType, String jobFullName, int buildNumber,
            String buildUrl, String releaseVersion, Instant jenkinsTimestamp,
            String conclusion) {}

    public ParsedEvent parse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode build = root.path("build");
            String phase = build.path("phase").asText("");
            String status = build.path("status").asText(null);
            String jobFullName = root.path("name").asText("");
            int buildNumber = build.path("number").asInt(0);
            String buildUrl = build.path("full_url").asText("");
            JsonNode params = build.path("parameters");
            String releaseVersion = params.has("RELEASE_VERSION")
                    ? params.path("RELEASE_VERSION").asText(null) : null;

            JenkinsEventType eventType = switch (phase.toUpperCase()) {
                case "STARTED" -> JenkinsEventType.JOB_STARTED;
                case "COMPLETED", "FINALIZED" -> JenkinsEventType.JOB_COMPLETED;
                default -> JenkinsEventType.JOB_COMPLETED;
            };

            return new ParsedEvent(eventType, jobFullName, buildNumber, buildUrl,
                    releaseVersion, Instant.now(), status);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Jenkins payload: " + e.getMessage(), e);
        }
    }
}
