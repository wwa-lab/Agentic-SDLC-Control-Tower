package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_assumption")
public class RequirementAssumptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(name = "assumption_text", columnDefinition = "CLOB", nullable = false)
    private String assumptionText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RequirementAssumptionEntity() {}

    public static RequirementAssumptionEntity create(String requirementId, String assumptionText, int displayOrder) {
        RequirementAssumptionEntity entity = new RequirementAssumptionEntity();
        entity.requirementId = requirementId;
        entity.assumptionText = assumptionText;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public String getRequirementId() { return requirementId; }
    public String getAssumptionText() { return assumptionText; }
    public int getDisplayOrder() { return displayOrder; }
}
