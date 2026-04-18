package com.sdlctower.domain.testingmanagement.events;

import org.springframework.stereotype.Component;

@Component("testingManagementChangeLogPublisher")
public class TestingManagementChangeLogPublisher {

    public void publish(String entryType, String referenceId) {
        // Read-only MVP: mutations land in a later phase.
    }
}
