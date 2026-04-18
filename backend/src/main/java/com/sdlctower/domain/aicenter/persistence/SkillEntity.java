package com.sdlctower.domain.aicenter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_skill_ws_key", columnList = "workspace_id, skill_key_code", unique = true),
        @Index(name = "idx_skill_ws_category", columnList = "workspace_id, category")
})
public class SkillEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "skill_key_code", nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(nullable = false)
    private String status;

    @Column(name = "default_autonomy", nullable = false)
    private String defaultAutonomy;

    @Column(nullable = false)
    private String owner;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(name = "input_contract", columnDefinition = "CLOB")
    private String inputContract;

    @Column(name = "output_contract", columnDefinition = "CLOB")
    private String outputContract;

    @Column(nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SkillEntity() {}

    public static SkillEntity create(String id, String workspaceId, String key, String name,
                                     String category, String subCategory, String status,
                                     String defaultAutonomy, String owner, String description,
                                     String inputContract, String outputContract, int version,
                                     Instant createdAt, Instant updatedAt) {
        SkillEntity e = new SkillEntity();
        e.id = id;
        e.workspaceId = workspaceId;
        e.key = key;
        e.name = name;
        e.category = category;
        e.subCategory = subCategory;
        e.status = status;
        e.defaultAutonomy = defaultAutonomy;
        e.owner = owner;
        e.description = description;
        e.inputContract = inputContract;
        e.outputContract = outputContract;
        e.version = version;
        e.createdAt = createdAt;
        e.updatedAt = updatedAt;
        return e;
    }

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getKey() { return key; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getStatus() { return status; }
    public String getDefaultAutonomy() { return defaultAutonomy; }
    public String getOwner() { return owner; }
    public String getDescription() { return description; }
    public String getInputContract() { return inputContract; }
    public String getOutputContract() { return outputContract; }
    public int getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
