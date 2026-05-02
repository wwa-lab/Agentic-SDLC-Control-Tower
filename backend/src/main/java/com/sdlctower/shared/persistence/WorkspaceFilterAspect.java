package com.sdlctower.shared.persistence;

import com.sdlctower.platform.workspace.WorkspaceContext;
import com.sdlctower.platform.workspace.WorkspaceContextHolder;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Around-advice on @Transactional methods: enables or disables the Hibernate
 * workspace_filter based on WorkspaceContextHolder state.
 */
@Aspect
@Component
public class WorkspaceFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceFilterAspect.class);
    private static final String FILTER_NAME = "workspace_filter";
    private static final String PARAM_NAME = "workspaceId";

    private final EntityManager entityManager;

    public WorkspaceFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Around("@within(org.springframework.transaction.annotation.Transactional) " +
            "|| @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object applyWorkspaceFilter(ProceedingJoinPoint pjp) throws Throwable {
        Session session = entityManager.unwrap(Session.class);
        boolean wasEnabled = session.getEnabledFilter(FILTER_NAME) != null;

        Optional<WorkspaceContext> ctx = WorkspaceContextHolder.maybeCurrent();
        boolean crossAccess = WorkspaceContextHolder.isCrossWorkspaceAccess();

        if (ctx.isPresent() && !crossAccess) {
            session.enableFilter(FILTER_NAME).setParameter(PARAM_NAME, ctx.get().workspaceId());
        } else {
            session.disableFilter(FILTER_NAME);
        }

        try {
            return pjp.proceed();
        } finally {
            if (wasEnabled) {
                ctx.ifPresent(c -> session.enableFilter(FILTER_NAME).setParameter(PARAM_NAME, c.workspaceId()));
            } else {
                session.disableFilter(FILTER_NAME);
            }
        }
    }
}
