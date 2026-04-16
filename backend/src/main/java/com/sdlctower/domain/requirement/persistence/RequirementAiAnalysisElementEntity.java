package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "requirement_ai_analysis_element")
public class RequirementAiAnalysisElementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "element_type", nullable = false)
    private String elementType;

    @Column(name = "element_text", columnDefinition = "CLOB", nullable = false)
    private String elementText;

    @Column(name = "related_requirement_id")
    private String relatedRequirementId;

    @Column(name = "numeric_value")
    private BigDecimal numericValue;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected RequirementAiAnalysisElementEntity() {}

    public static RequirementAiAnalysisElementEntity create(
            Long analysisId,
            String elementType,
            String elementText,
            String relatedRequirementId,
            BigDecimal numericValue,
            int displayOrder
    ) {
        RequirementAiAnalysisElementEntity entity = new RequirementAiAnalysisElementEntity();
        entity.analysisId = analysisId;
        entity.elementType = elementType;
        entity.elementText = elementText;
        entity.relatedRequirementId = relatedRequirementId;
        entity.numericValue = numericValue;
        entity.displayOrder = displayOrder;
        return entity;
    }

    public Long getAnalysisId() { return analysisId; }
    public String getElementType() { return elementType; }
    public String getElementText() { return elementText; }
    public String getRelatedRequirementId() { return relatedRequirementId; }
    public BigDecimal getNumericValue() { return numericValue; }
    public int getDisplayOrder() { return displayOrder; }
}
