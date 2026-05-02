package com.sdlctower.platform.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.shared.audit.AuditEventService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {

    private final SupportRequestRepository repository;
    private final ObjectMapper objectMapper;
    private final AuditEventService auditEventService;
    private final String jiraBaseUrl;
    private final String jiraProjectKey;
    private final boolean jiraAvailable;

    public SupportService(
            SupportRequestRepository repository,
            ObjectMapper objectMapper,
            AuditEventService auditEventService,
            @Value("${app.support.jira-base-url:https://jira.company.com}") String jiraBaseUrl,
            @Value("${app.support.jira-project-key:SDLC}") String jiraProjectKey,
            @Value("${app.support.jira-available:true}") boolean jiraAvailable
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditEventService = auditEventService;
        this.jiraBaseUrl = jiraBaseUrl;
        this.jiraProjectKey = jiraProjectKey;
        this.jiraAvailable = jiraAvailable;
    }

    @Transactional
    public SupportRequestResultDto contact(SupportRequestDto request) {
        if (request.title() == null || request.title().isBlank()
                || request.category() == null || request.category().isBlank()
                || request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("title, category, and description are required");
        }
        Instant now = Instant.now();
        LocalDate requestDate = LocalDate.now();
        long current = repository.countByRequestDate(requestDate) + 1;
        String date = requestDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String requestId = "support-req-" + date + "-" + String.format("%04d", current);
        SupportRequestEntity entity = repository.save(new SupportRequestEntity(requestId, requestDate, request, serialize(request), now));
        if (!jiraAvailable) {
            entity.markPendingRetry(now);
            recordAudit("support.request.queued", request, entity, now);
            return new SupportRequestResultDto(requestId, "pending", null, null);
        }
        String jiraKey = jiraProjectKey + "-" + (1200 + current);
        String jiraUrl = jiraBaseUrl + "/browse/" + jiraKey;
        entity.markCreated(jiraKey, jiraUrl, now);
        recordAudit("support.request.created", request, entity, now);
        return new SupportRequestResultDto(requestId, entity.getStatus(), entity.getJiraKey(), entity.getJiraUrl());
    }

    private String serialize(SupportRequestDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize support request", ex);
        }
    }

    private void recordAudit(String event, SupportRequestDto request, SupportRequestEntity entity, Instant now) {
        auditEventService.record(new AuditEventService.AuditEvent(
                event,
                request.reporterStaffId() == null ? request.reporterMode() : request.reporterStaffId(),
                entity.getRequestId(),
                now,
                Map.of(
                        "status", entity.getStatus(),
                        "category", request.category(),
                        "route", request.route() == null ? "" : request.route(),
                        "jiraKey", entity.getJiraKey() == null ? "" : entity.getJiraKey()
                )
        ));
    }
}
