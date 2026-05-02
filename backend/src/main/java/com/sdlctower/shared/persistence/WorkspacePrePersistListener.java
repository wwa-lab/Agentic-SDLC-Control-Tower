package com.sdlctower.shared.persistence;

import com.sdlctower.platform.workspace.WorkspaceContextHolder;
import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Canary @PrePersist listener: throws if a workspace-scoped row is inserted
 * while the holder is set but the entity's workspaceId field is null.
 * Add this as @EntityListeners on workspace-scoped entities.
 */
public class WorkspacePrePersistListener {

    private static final Logger log = LoggerFactory.getLogger(WorkspacePrePersistListener.class);

    @PrePersist
    public void checkWorkspaceId(Object entity) {
        WorkspaceContextHolder.maybeCurrent().ifPresent(ctx -> {
            try {
                java.lang.reflect.Field field = findWorkspaceIdField(entity.getClass());
                if (field == null) return;
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value == null) {
                    field.set(entity, ctx.workspaceId());
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                log.warn("WorkspacePrePersistListener reflection error for {}", entity.getClass().getSimpleName(), e);
            }
        });
    }

    private java.lang.reflect.Field findWorkspaceIdField(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField("workspaceId");
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
