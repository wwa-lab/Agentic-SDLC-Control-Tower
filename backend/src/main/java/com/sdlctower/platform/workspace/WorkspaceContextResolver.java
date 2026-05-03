package com.sdlctower.platform.workspace;

import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.platform.auth.ScopeDto;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resolves and caches PlatformWorkspaceEntity → WorkspaceContext.
 * Cache TTL is 60 seconds; cache is shared across threads.
 */
@Component
public class WorkspaceContextResolver {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceContextResolver.class);
    private static final long CACHE_TTL_SECONDS = 60;

    private final PlatformWorkspaceRepository workspaceRepo;
    private final WorkspaceKeyAliasRepository aliasRepo;

    private final Map<String, CacheEntry> byIdCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> byKeyCache = new ConcurrentHashMap<>();

    public WorkspaceContextResolver(PlatformWorkspaceRepository workspaceRepo,
                                    WorkspaceKeyAliasRepository aliasRepo) {
        this.workspaceRepo = workspaceRepo;
        this.aliasRepo = aliasRepo;
    }

    public WorkspaceContext resolveById(String workspaceId, boolean demoMode) {
        CacheEntry cached = byIdCache.get(workspaceId);
        if (cached != null && !cached.isExpired()) {
            return cached.context;
        }
        PlatformWorkspaceEntity entity = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
        WorkspaceContext ctx = toContext(entity, demoMode);
        byIdCache.put(workspaceId, new CacheEntry(ctx));
        return ctx;
    }

    /** Returns the live key for the given input key, or empty if the key is an alias. */
    public Optional<String> resolveAlias(String key) {
        return aliasRepo.findByFormerKeyAndExpiresAtAfter(key, Instant.now())
                .map(WorkspaceKeyAliasEntity::getWorkspaceId)
                .flatMap(workspaceId -> workspaceRepo.findById(workspaceId))
                .map(PlatformWorkspaceEntity::getWorkspaceKey);
    }

    public Optional<WorkspaceListItemDto> resolveByKey(String key) {
        CacheEntry cached = byKeyCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(toListItem(cached.context));
        }
        return workspaceRepo.findByWorkspaceKey(key)
                .map(entity -> {
                    WorkspaceContext ctx = toContext(entity, false);
                    byKeyCache.put(key, new CacheEntry(ctx));
                    return toListItem(ctx);
                });
    }

    public boolean hasScope(CurrentUserDto user, String workspaceId) {
        if (user == null || user.scopes() == null) return false;
        for (ScopeDto scope : user.scopes()) {
            if (scopeCoversWorkspace(scope, workspaceId)) return true;
        }
        return false;
    }

    private boolean scopeCoversWorkspace(ScopeDto scope, String workspaceId) {
        if (scope == null) return false;
        return switch (scope.scopeType()) {
            case "platform" -> "*".equals(scope.scopeId());
            case "workspace" -> workspaceId.equals(scope.scopeId());
            default -> false;
        };
    }

    private WorkspaceContext toContext(PlatformWorkspaceEntity e, boolean demoMode) {
        return new WorkspaceContext(
                e.getId(),
                e.getWorkspaceKey(),
                e.getName(),
                e.getApplicationId(),
                e.getSnowGroupId(),
                e.getProfileId() != null ? e.getProfileId() : "standard-java-sdd",
                demoMode
        );
    }

    private WorkspaceListItemDto toListItem(WorkspaceContext ctx) {
        return new WorkspaceListItemDto(
                ctx.workspaceId(),
                ctx.workspaceKey(),
                ctx.workspaceName(),
                ctx.applicationId(),
                ctx.snowGroupId(),
                ctx.profileId()
        );
    }

    private static final class CacheEntry {
        final WorkspaceContext context;
        final long expiresEpoch;

        CacheEntry(WorkspaceContext context) {
            this.context = context;
            this.expiresEpoch = Instant.now().getEpochSecond() + CACHE_TTL_SECONDS;
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresEpoch;
        }
    }
}
