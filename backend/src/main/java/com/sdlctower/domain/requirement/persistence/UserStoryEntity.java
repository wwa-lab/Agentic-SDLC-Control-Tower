package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_story")
public class UserStoryEntity {

    @Id
    private String id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status;

    @Column(name = "spec_id")
    private String specId;

    @Column(name = "spec_status")
    private String specStatus;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected UserStoryEntity() {}

    public static UserStoryEntity create(
            String id,
            String requirementId,
            String title,
            String status,
            String specId,
            String specStatus,
            int displayOrder
    ) {
        UserStoryEntity entity = new UserStoryEntity();
        entity.id = id;
        entity.requirementId = requirementId;
        entity.title = title;
        entity.status = status;
        entity.specId = specId;
        entity.specStatus = specStatus;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public String getId() { return id; }
    public String getRequirementId() { return requirementId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getSpecId() { return specId; }
    public String getSpecStatus() { return specStatus; }
    public int getDisplayOrder() { return displayOrder; }
}
