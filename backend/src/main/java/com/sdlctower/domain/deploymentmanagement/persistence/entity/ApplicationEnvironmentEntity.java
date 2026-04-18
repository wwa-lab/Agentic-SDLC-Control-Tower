package com.sdlctower.domain.deploymentmanagement.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dp_application_environment")
public class ApplicationEnvironmentEntity {

    @Id private String id;
    @Column(name = "application_id", nullable = false) private String applicationId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String kind;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected ApplicationEnvironmentEntity() {}

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getName() { return name; }
    public String getKind() { return kind; }
}
