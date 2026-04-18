package com.sdlctower.domain.testingmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "test_case_req_link")
public class TestCaseReqLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private String caseId;

    @Column(name = "req_id", nullable = false)
    private String reqId;

    @Column(name = "link_status", nullable = false)
    private String linkStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TestCaseReqLinkEntity() {}

    public Long getId() { return id; }
    public String getCaseId() { return caseId; }
    public String getReqId() { return reqId; }
    public String getLinkStatus() { return linkStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
