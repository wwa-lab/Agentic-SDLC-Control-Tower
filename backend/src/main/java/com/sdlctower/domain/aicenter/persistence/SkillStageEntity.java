package com.sdlctower.domain.aicenter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "skill_stage", indexes = {
        @Index(name = "idx_skill_stage_skill", columnList = "skill_id")
})
public class SkillStageEntity {

    @Id
    private String id;

    @Column(name = "skill_id", nullable = false)
    private String skillId;

    @Column(name = "stage_key", nullable = false)
    private String stageKey;

    protected SkillStageEntity() {}

    public static SkillStageEntity create(String id, String skillId, String stageKey) {
        SkillStageEntity e = new SkillStageEntity();
        e.id = id;
        e.skillId = skillId;
        e.stageKey = stageKey;
        return e;
    }

    public String getId() { return id; }
    public String getSkillId() { return skillId; }
    public String getStageKey() { return stageKey; }
}
