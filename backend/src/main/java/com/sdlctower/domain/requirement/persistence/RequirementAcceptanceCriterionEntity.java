package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_acceptance_criterion")
public class RequirementAcceptanceCriterionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(name = "criterion_key", nullable = false)
    private String criterionKey;

    @Column(name = "criterion_text", columnDefinition = "CLOB", nullable = false)
    private String criterionText;

    @Column(name = "is_met", nullable = false)
    private boolean met;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RequirementAcceptanceCriterionEntity() {}

    public static RequirementAcceptanceCriterionEntity create(
            String requirementId,
            String criterionKey,
            String criterionText,
            boolean met,
            int displayOrder
    ) {
        RequirementAcceptanceCriterionEntity entity = new RequirementAcceptanceCriterionEntity();
        entity.requirementId = requirementId;
        entity.criterionKey = criterionKey;
        entity.criterionText = criterionText;
        entity.met = met;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public String getRequirementId() { return requirementId; }
    public String getCriterionKey() { return criterionKey; }
    public String getCriterionText() { return criterionText; }
    public boolean isMet() { return met; }
    public int getDisplayOrder() { return displayOrder; }
}
