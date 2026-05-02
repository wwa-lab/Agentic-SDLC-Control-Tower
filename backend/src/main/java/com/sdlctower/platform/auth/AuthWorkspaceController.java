package com.sdlctower.platform.auth;

import com.sdlctower.platform.workspace.WorkspaceContextDto;
import com.sdlctower.platform.workspace.WorkspaceContextResolver;
import com.sdlctower.platform.workspace.WorkspaceCrossAccessReason;
import com.sdlctower.platform.workspace.WorkspaceContextHolder;
import com.sdlctower.platform.workspace.WorkspaceListItemDto;
import com.sdlctower.platform.workspace.PlatformWorkspaceRepository;
import com.sdlctower.platform.workspace.WorkspaceKeyAliasRepository;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthWorkspaceController {

    private final AuthService authService;
    private final WorkspaceContextResolver resolver;
    private final PlatformWorkspaceRepository workspaceRepo;
    private final WorkspaceKeyAliasRepository aliasRepo;

    public AuthWorkspaceController(AuthService authService,
                                   WorkspaceContextResolver resolver,
                                   PlatformWorkspaceRepository workspaceRepo,
                                   WorkspaceKeyAliasRepository aliasRepo) {
        this.authService = authService;
        this.resolver = resolver;
        this.workspaceRepo = workspaceRepo;
        this.aliasRepo = aliasRepo;
    }

    @PostMapping(ApiConstants.AUTH_WORKSPACE_SWITCH)
    public ResponseEntity<ApiResponse<WorkspaceListItemDto>> switchWorkspace(
            @Valid @RequestBody SwitchWorkspaceRequest req,
            HttpServletRequest http) {
        CurrentUserDto user = authService.requireUser(http);
        if (!resolver.hasScope(user, req.workspaceId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("WORKSPACE_SCOPE_REQUIRED"));
        }
        Optional<WorkspaceListItemDto> item = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
                WorkspaceCrossAccessReason.AUTH_WORKSPACE_RESOLVE,
                () -> resolver.resolveByKey(
                        workspaceRepo.findById(req.workspaceId())
                                .map(w -> w.getWorkspaceKey())
                                .orElseThrow()
                )
        );
        return item.map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("WORKSPACE_NOT_FOUND")));
    }

    @GetMapping(ApiConstants.AUTH_WORKSPACES_LIST)
    public ApiResponse<List<WorkspaceListItemDto>> listMyWorkspaces(HttpServletRequest http) {
        CurrentUserDto user = authService.requireUser(http);
        List<WorkspaceListItemDto> items = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
                WorkspaceCrossAccessReason.AUTH_WORKSPACE_RESOLVE,
                () -> workspaceRepo.findAccessibleByStaffId(user.staffId())
                        .stream()
                        .map(w -> new WorkspaceListItemDto(
                                w.getId(), w.getWorkspaceKey(), w.getName(),
                                w.getApplicationId(), w.getSnowGroupId(),
                                w.getProfileId() != null ? w.getProfileId() : "standard-java-sdd"))
                        .toList()
        );
        return ApiResponse.ok(items);
    }

    @GetMapping(ApiConstants.AUTH_WORKSPACES_BY_KEY)
    public ResponseEntity<ApiResponse<WorkspaceListItemDto>> resolveByKey(
            @PathVariable String key,
            HttpServletRequest http) {
        authService.requireUser(http);

        // Check alias first (within cross-workspace access)
        Optional<String> liveKey = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
                WorkspaceCrossAccessReason.AUTH_WORKSPACE_RESOLVE,
                () -> aliasRepo.findByFormerKeyAndExpiresAtAfter(key, Instant.now())
                        .map(alias -> workspaceRepo.findById(alias.getWorkspaceId())
                                .map(w -> w.getWorkspaceKey())
                                .orElse(null))
        );

        if (liveKey.isPresent()) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(URI.create(ApiConstants.AUTH_WORKSPACES_BY_KEY.replace("{key}", liveKey.get())))
                    .body(null);
        }

        Optional<WorkspaceListItemDto> item = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
                WorkspaceCrossAccessReason.AUTH_WORKSPACE_RESOLVE,
                () -> resolver.resolveByKey(key)
        );

        return item.map(dto -> ResponseEntity.ok(ApiResponse.ok(dto)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("WORKSPACE_NOT_FOUND")));
    }
}
