package com.sdlctower.domain.testingmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sdlctower.domain.testingmanagement.service.CaseDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class CaseDetailServiceTest {

    @Autowired
    private CaseDetailService caseDetailService;

    @Test
    void linkedRequirementChipsExposeGreenAmberRedAndGreyStates() {
        var aggregate = caseDetailService.load("case-color-1102");
        assertEquals("GREEN", aggregate.detail().data().linkedReqs().get(0).chipColor().name());
        assertEquals("AMBER", aggregate.detail().data().linkedReqs().get(1).chipColor().name());
        assertEquals("RED", aggregate.detail().data().linkedReqs().get(2).chipColor().name());
        assertEquals("GREY", aggregate.detail().data().linkedReqs().get(3).chipColor().name());
    }
}
