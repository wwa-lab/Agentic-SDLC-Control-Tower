package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_workspace_summary")
public class AiWorkspaceSummaryEntity {

    @Id
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "repo_id")
    private String repoId;

    @Column(name = "skill_version")
    private String skillVersion;

    private String status;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Lob
    private String narrative;

    @Lob
    @Column(name = "evidence_json")
    private String evidenceJson;

    protected AiWorkspaceSummaryEntity() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getRepoId() { return repoId; }
    public String getSkillVersion() { return skillVersion; }
    public String getStatus() { return status; }
    public Instant getGeneratedAt() { return generatedAt; }
    public String getNarrative() { return narrative; }
    public String getEvidenceJson() { return evidenceJson; }
}
