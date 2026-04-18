package com.sdlctower.domain.codebuildmanagement.events;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.ChangeLogEntryType;
import com.sdlctower.domain.codebuildmanagement.persistence.CodeBuildChangeLogEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.CodeBuildChangeLogRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publishes change log entries for auditable Code & Build Management state transitions.
 */
@Component("codeBuildManagementChangeLogPublisher")
public class CodeBuildChangeLogPublisher {

    private static final Logger log = LoggerFactory.getLogger(CodeBuildChangeLogPublisher.class);

    private final CodeBuildChangeLogRepository changeLogRepository;

    public CodeBuildChangeLogPublisher(CodeBuildChangeLogRepository changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    public void publish(String entityType, String entityId, ChangeLogEntryType entryType, String actorId, String detail) {
        CodeBuildChangeLogEntity entry = CodeBuildChangeLogEntity.create(
                "changelog-" + UUID.randomUUID().toString().substring(0, 8),
                entityType,
                entityId,
                entryType.name(),
                actorId,
                detail,
                Instant.now()
        );
        changeLogRepository.save(entry);
        log.info("Change log: {} {} {} by {}", entryType, entityType, entityId, actorId);
    }
}
