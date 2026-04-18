package com.sdlctower.domain.codebuildmanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_pr_review_note")
public class AiPrReviewNoteEntity {

    @Id
    private String id;

    @Column(name = "review_id", nullable = false)
    private String reviewId;

    private String severity;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "start_line")
    private Integer startLine;

    @Column(name = "end_line")
    private Integer endLine;

    @Column(length = 4096)
    private String message;

    protected AiPrReviewNoteEntity() {}

    public String getId() { return id; }
    public String getReviewId() { return reviewId; }
    public String getSeverity() { return severity; }
    public String getFilePath() { return filePath; }
    public Integer getStartLine() { return startLine; }
    public Integer getEndLine() { return endLine; }
    public String getMessage() { return message; }
}
