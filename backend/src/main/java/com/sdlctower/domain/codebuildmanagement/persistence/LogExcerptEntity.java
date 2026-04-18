package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "log_excerpt")
public class LogExcerptEntity {

    @Id
    private String id;

    @Column(name = "step_id", nullable = false)
    private String stepId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String text;

    @Column(name = "byte_count")
    private int byteCount;

    @Column(name = "external_url")
    private String externalUrl;

    protected LogExcerptEntity() {}

    public String getId() { return id; }
    public String getStepId() { return stepId; }
    public String getText() { return text; }
    public int getByteCount() { return byteCount; }
    public String getExternalUrl() { return externalUrl; }
}
