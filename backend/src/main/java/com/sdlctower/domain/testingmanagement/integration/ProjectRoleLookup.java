package com.sdlctower.domain.testingmanagement.integration;

import java.util.List;

public interface ProjectRoleLookup {

    List<String> rolesForProject(String projectId, String memberId);
}
