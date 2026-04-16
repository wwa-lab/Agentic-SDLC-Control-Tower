package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_constraint")
public class RequirementConstraintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(name = "constraint_text", columnDefinition = "CLOB", nullable = false)
    private String constraintText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RequirementConstraintEntity() {}

    public static RequirementConstraintEntity create(String requirementId, String constraintText, int displayOrder) {
        RequirementConstraintEntity entity = new RequirementConstraintEntity();
        entity.requirementId = requirementId;
        entity.constraintText = constraintText;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public String getRequirementId() { return requirementId; }
    public String getConstraintText() { return constraintText; }
    public int getDisplayOrder() { return displayOrder; }
}
