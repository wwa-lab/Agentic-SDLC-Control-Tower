package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_ai_analysis")
public class RequirementAiAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false, unique = true)
    private String requirementId;

    @Column(name = "completeness_score", nullable = false)
    private int completenessScore;

    @Column(name = "impact_assessment", columnDefinition = "CLOB", nullable = false)
    private String impactAssessment;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    protected RequirementAiAnalysisEntity() {}

    public static RequirementAiAnalysisEntity create(
            String requirementId,
            int completenessScore,
            String impactAssessment,
            Instant analyzedAt
    ) {
        RequirementAiAnalysisEntity entity = new RequirementAiAnalysisEntity();
        entity.requirementId = requirementId;
        entity.completenessScore = completenessScore;
        entity.impactAssessment = impactAssessment;
        entity.analyzedAt = analyzedAt;
        return entity;
    }

    public Long getId() { return id; }
    public String getRequirementId() { return requirementId; }
    public int getCompletenessScore() { return completenessScore; }
    public String getImpactAssessment() { return impactAssessment; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}
