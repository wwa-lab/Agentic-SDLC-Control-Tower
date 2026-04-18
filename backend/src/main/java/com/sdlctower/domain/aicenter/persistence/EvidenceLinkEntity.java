package com.sdlctower.domain.aicenter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "evidence_link", indexes = {
        @Index(name = "idx_ev_execution", columnList = "execution_id, position")
})
public class EvidenceLinkEntity {

    @Id
    private String id;

    @Column(name = "execution_id", nullable = false)
    private String executionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false)
    private int position;

    protected EvidenceLinkEntity() {}

    public static EvidenceLinkEntity create(String id, String executionId, String title,
                                            String type, String sourceSystem, String url,
                                            int position) {
        EvidenceLinkEntity e = new EvidenceLinkEntity();
        e.id = id;
        e.executionId = executionId;
        e.title = title;
        e.type = type;
        e.sourceSystem = sourceSystem;
        e.url = url;
        e.position = position;
        return e;
    }

    public String getId() { return id; }
    public String getExecutionId() { return executionId; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getSourceSystem() { return sourceSystem; }
    public String getUrl() { return url; }
    public int getPosition() { return position; }
}
