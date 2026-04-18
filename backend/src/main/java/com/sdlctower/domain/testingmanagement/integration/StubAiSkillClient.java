package com.sdlctower.domain.testingmanagement.integration;

import java.util.List;
import org.springframework.stereotype.Component;

@Component("testingManagementStubAiSkillClient")
public class StubAiSkillClient implements AiSkillClient {

    @Override
    public List<String> draftCaseTitles(String planId, String reqId) {
        return List.of(
                "AI draft coverage for " + reqId,
                "Negative path review for " + planId
        );
    }
}
