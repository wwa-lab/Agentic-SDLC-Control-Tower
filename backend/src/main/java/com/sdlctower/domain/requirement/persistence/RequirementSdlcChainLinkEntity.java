package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_sdlc_chain_link")
public class RequirementSdlcChainLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private String requirementId;

    @Column(name = "artifact_type", nullable = false)
    private String artifactType;

    @Column(name = "artifact_id", nullable = false)
    private String artifactId;

    @Column(name = "artifact_title", nullable = false)
    private String artifactTitle;

    @Column(name = "route_path", nullable = false)
    private String routePath;

    @Column(name = "link_order", nullable = false)
    private int linkOrder;

    protected RequirementSdlcChainLinkEntity() {}

    public static RequirementSdlcChainLinkEntity create(
            String requirementId,
            String artifactType,
            String artifactId,
            String artifactTitle,
            String routePath,
            int linkOrder
    ) {
        RequirementSdlcChainLinkEntity entity = new RequirementSdlcChainLinkEntity();
        entity.requirementId = requirementId;
        entity.artifactType = artifactType;
        entity.artifactId = artifactId;
        entity.artifactTitle = artifactTitle;
        entity.routePath = routePath;
        entity.linkOrder = linkOrder;
        return entity;
    }

    public String getRequirementId() { return requirementId; }
    public String getArtifactType() { return artifactType; }
    public String getArtifactId() { return artifactId; }
    public String getArtifactTitle() { return artifactTitle; }
    public String getRoutePath() { return routePath; }
    public int getLinkOrder() { return linkOrder; }
}
