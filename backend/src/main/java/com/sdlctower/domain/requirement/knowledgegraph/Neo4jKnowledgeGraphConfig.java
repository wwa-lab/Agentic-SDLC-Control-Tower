package com.sdlctower.domain.requirement.knowledgegraph;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.requirement-knowledge-graph", name = "provider", havingValue = "neo4j")
public class Neo4jKnowledgeGraphConfig {

    @Bean
    Driver neo4jDriver(KnowledgeGraphProperties properties) {
        return GraphDatabase.driver(
                properties.neo4j().uri(),
                AuthTokens.basic(properties.neo4j().username(), properties.neo4j().password())
        );
    }
}
