package com.sdlctower.domain.testingmanagement.integration;

import java.util.List;

public interface AiSkillClient {

    List<String> draftCaseTitles(String planId, String reqId);
}
