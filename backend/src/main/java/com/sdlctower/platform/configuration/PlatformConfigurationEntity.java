package com.sdlctower.platform.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "PLATFORM_CONFIGURATION")
public class PlatformConfigurationEntity {

    @Id
    private String id;

    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String kind;

    @Column(name = "scope_type", nullable = false)
    private String scopeType;

    @Column(name = "scope_id", nullable = false)
    private String scopeId;

    @Column(name = "parent_id")
    private String parentId;

    @Column(nullable = false)
    private String status;

    @Lob
    private String body;

    @Column(name = "has_drift", nullable = false)
    private Boolean hasDrift;

    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeId() { return scopeId; }
    public void setScopeId(String scopeId) { this.scopeId = scopeId; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Boolean getHasDrift() { return hasDrift; }
    public void setHasDrift(Boolean hasDrift) { this.hasDrift = hasDrift; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}
