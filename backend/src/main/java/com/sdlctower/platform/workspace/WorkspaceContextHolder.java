package com.sdlctower.platform.workspace;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * ThreadLocal carrier for the current request's WorkspaceContext.
 * Set by WorkspaceContextInterceptor in preHandle; cleared in afterCompletion.
 * Cross-workspace access (webhooks, fleet reports) disables the Hibernate filter
 * by setting the crossWorkspaceAccess flag instead of populating the context.
 */
public final class WorkspaceContextHolder {

    private static final ThreadLocal<WorkspaceContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CROSS_ACCESS = ThreadLocal.withInitial(() -> false);

    private WorkspaceContextHolder() {}

    public static WorkspaceContext current() {
        WorkspaceContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new MissingWorkspaceContextException();
        }
        return ctx;
    }

    public static Optional<WorkspaceContext> maybeCurrent() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void set(WorkspaceContext ctx) {
        CONTEXT.set(ctx);
        CROSS_ACCESS.set(false);
    }

    public static void clear() {
        CONTEXT.remove();
        CROSS_ACCESS.remove();
    }

    public static boolean isCrossWorkspaceAccess() {
        return Boolean.TRUE.equals(CROSS_ACCESS.get());
    }

    /**
     * Execute {@code action} with the workspace filter disabled.
     * Reason codes are constrained to WorkspaceCrossAccessReason values and logged.
     */
    public static <T> T runWithCrossWorkspaceAccess(WorkspaceCrossAccessReason reason, Supplier<T> action) {
        boolean previous = Boolean.TRUE.equals(CROSS_ACCESS.get());
        CROSS_ACCESS.set(true);
        try {
            return action.get();
        } finally {
            CROSS_ACCESS.set(previous);
        }
    }
}
