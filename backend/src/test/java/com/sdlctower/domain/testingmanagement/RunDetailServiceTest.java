package com.sdlctower.domain.testingmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sdlctower.domain.testingmanagement.service.RunDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class RunDetailServiceTest {

    @Autowired
    private RunDetailService runDetailService;

    @Test
    void failureExcerptIsTrimmedToHardLimit() {
        var aggregate = runDetailService.loadAggregate("run-auth-001");
        String excerpt = aggregate.caseResults().data().get(1).failureExcerpt();
        assertEquals(4096, excerpt.length());
        assertTrue(excerpt.startsWith("3ds timeout"));
    }
}
