package com.sdlctower.domain.projectmanagement.policy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ProjectManagementActorResolver {

    public static final String ACTOR_ID_HEADER = "X-PM-ACTOR-ID";
    public static final String WORKSPACE_ADMIN_HEADER = "X-PM-WORKSPACE-ADMIN";
    public static final String APPLICATION_OWNER_HEADER = "X-PM-APPLICATION-OWNER";
    public static final String AUDITOR_HEADER = "X-PM-AUDITOR";

    public Actor currentActor() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return new Actor("u-007", false, false, false);
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String actorId = headerOrDefault(request, ACTOR_ID_HEADER, "u-007");
        return new Actor(
                actorId,
                Boolean.parseBoolean(headerOrDefault(request, WORKSPACE_ADMIN_HEADER, "false")),
                Boolean.parseBoolean(headerOrDefault(request, APPLICATION_OWNER_HEADER, "false")),
                Boolean.parseBoolean(headerOrDefault(request, AUDITOR_HEADER, "false"))
        );
    }

    private String headerOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public record Actor(
            String memberId,
            boolean workspaceAdmin,
            boolean applicationOwner,
            boolean auditor
    ) {
        public boolean isElevated() {
            return workspaceAdmin || applicationOwner;
        }
    }
}
