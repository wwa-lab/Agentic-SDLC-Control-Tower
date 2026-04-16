package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_spec")
public class RequirementSpecEntity {

    @Id
    private String id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String version;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RequirementSpecEntity() {}

    public static RequirementSpecEntity create(
            String id,
            String requirementId,
            String title,
            String status,
            String version,
            int displayOrder
    ) {
        RequirementSpecEntity entity = new RequirementSpecEntity();
        entity.id = id;
        entity.requirementId = requirementId;
        entity.title = title;
        entity.status = status;
        entity.version = version;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public String getId() { return id; }
    public String getRequirementId() { return requirementId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getVersion() { return version; }
    public int getDisplayOrder() { return displayOrder; }
}
