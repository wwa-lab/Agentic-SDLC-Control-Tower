package com.sdlctower.shared.audit;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    public void record(AuditEvent event) {
        log.info("audit_event event={} actor={} subject={} at={} attrs={}",
                event.event(), event.actor(), event.subject(), event.at(), event.attributes());
    }

    public record AuditEvent(
            String event,
            String actor,
            String subject,
            Instant at,
            Map<String, Object> attributes
    ) {}
}
