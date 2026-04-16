package com.sdlctower.platform.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for workspace context.
 * Internal to the persistence layer — never exposed directly in API responses.
 * Use {@link WorkspaceContextDto} for the API contract.
 */
@Entity
@Table(name = "workspace_context")
public class WorkspaceContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_name", nullable = false)
    private String workspace;

    @Column(name = "application_name", nullable = false)
    private String application;

    @Column(name = "snow_group")
    private String snowGroup;

    @Column(name = "project_name")
    private String project;

    @Column(name = "environment_name")
    private String environment;

    /** Required by JPA. */
    protected WorkspaceContext() {}

    /** Factory method for programmatic creation. */
    public static WorkspaceContext create(
            String workspace,
            String application,
            String snowGroup,
            String project,
            String environment
    ) {
        WorkspaceContext ctx = new WorkspaceContext();
        ctx.workspace = workspace;
        ctx.application = application;
        ctx.snowGroup = snowGroup;
        ctx.project = project;
        ctx.environment = environment;
        return ctx;
    }

    public Long getId() { return id; }
    public String getWorkspace() { return workspace; }
    public String getApplication() { return application; }
    public String getSnowGroup() { return snowGroup; }
    public String getProject() { return project; }
    public String getEnvironment() { return environment; }
}
