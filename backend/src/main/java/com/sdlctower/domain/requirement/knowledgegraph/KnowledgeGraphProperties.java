package com.sdlctower.domain.requirement.knowledgegraph;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.requirement-knowledge-graph")
public record KnowledgeGraphProperties(
        String provider,
        String manifestRoot,
        String structuredRepoName,
        Neo4j neo4j
) {
    public KnowledgeGraphProperties {
        provider = blankToDefault(provider, "profile");
        manifestRoot = blankToDefault(manifestRoot, "");
        structuredRepoName = blankToDefault(structuredRepoName, "sdd-knowledge-graph");
        neo4j = neo4j == null ? new Neo4j(null, null, null, null) : neo4j;
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record Neo4j(String uri, String username, String password, String database) {
        public Neo4j {
            uri = blankToDefault(uri, "bolt://localhost:7687");
            username = blankToDefault(username, "neo4j");
            password = blankToDefault(password, "local-dev-password");
            database = blankToDefault(database, "neo4j");
        }
    }
}
