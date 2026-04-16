package com.sdlctower.domain.requirement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "requirement_import_audit")
public class RequirementImportAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id")
    private String requirementId;

    @Column(name = "source_format", nullable = false)
    private String sourceFormat;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "skill_id", nullable = false)
    private String skillId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "outcome", nullable = false)
    private String outcome;

    protected RequirementImportAuditEntity() {}

    public static RequirementImportAuditEntity create(
            String requirementId,
            String sourceFormat,
            String fileName,
            Long fileSize,
            String skillId,
            String username,
            Instant createdAt,
            String outcome
    ) {
        RequirementImportAuditEntity entity = new RequirementImportAuditEntity();
        entity.requirementId = requirementId;
        entity.sourceFormat = sourceFormat;
        entity.fileName = fileName;
        entity.fileSize = fileSize;
        entity.skillId = skillId;
        entity.username = username;
        entity.createdAt = createdAt;
        entity.outcome = outcome;
        return entity;
    }
}
