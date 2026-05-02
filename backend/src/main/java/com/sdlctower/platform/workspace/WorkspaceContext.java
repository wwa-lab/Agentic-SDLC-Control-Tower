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

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "application_name", nullable = false)
    private String application;

    @Column(name = "snow_group_id")
    private String snowGroupId;

    @Column(name = "snow_group")
    private String snowGroup;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "project_name")
    private String project;

    @Column(name = "environment_name")
    private String environment;

    @Column(name = "demo_mode", nullable = false)
    private boolean demoMode;

    /** Required by JPA. */
    protected WorkspaceContext() {}

    /** Factory method for programmatic creation. */
    public static WorkspaceContext create(
            String workspaceId,
            String workspace,
            String applicationId,
            String application,
            String snowGroupId,
            String snowGroup,
            String projectId,
            String project,
            String environment,
            boolean demoMode
    ) {
        WorkspaceContext ctx = new WorkspaceContext();
        ctx.workspaceId = workspaceId;
        ctx.workspace = workspace;
        ctx.applicationId = applicationId;
        ctx.application = application;
        ctx.snowGroupId = snowGroupId;
        ctx.snowGroup = snowGroup;
        ctx.projectId = projectId;
        ctx.project = project;
        ctx.environment = environment;
        ctx.demoMode = demoMode;
        return ctx;
    }

    public Long getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getWorkspace() { return workspace; }
    public String getApplicationId() { return applicationId; }
    public String getApplication() { return application; }
    public String getSnowGroupId() { return snowGroupId; }
    public String getSnowGroup() { return snowGroup; }
    public String getProjectId() { return projectId; }
    public String getProject() { return project; }
    public String getEnvironment() { return environment; }
    public boolean isDemoMode() { return demoMode; }
}
