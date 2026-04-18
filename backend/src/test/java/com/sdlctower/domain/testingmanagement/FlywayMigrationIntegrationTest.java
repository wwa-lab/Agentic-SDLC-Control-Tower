package com.sdlctower.domain.testingmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class FlywayMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testingManagementTablesExistAfterMigration() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name in ('TEST_PLAN', 'TEST_CASE', 'TEST_RUN', 'TEST_CASE_REQ_LINK')",
                Integer.class
        );
        assertEquals(4, count);
    }
}
