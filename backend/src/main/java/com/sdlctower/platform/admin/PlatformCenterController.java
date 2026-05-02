package com.sdlctower.platform.admin;

import com.sdlctower.platform.access.AssignRoleRequest;
import com.sdlctower.platform.access.PlatformAccessService;
import com.sdlctower.platform.access.PlatformUserDto;
import com.sdlctower.platform.access.RoleAssignmentDto;
import com.sdlctower.platform.access.UpsertPlatformUserRequest;
import com.sdlctower.platform.audit.AuditQueryService;
import com.sdlctower.platform.audit.AuditRecordDto;
import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.platform.auth.CurrentUserDto;
import com.sdlctower.platform.shared.CursorPageDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformCenterController {

    private final PlatformCatalogService catalogService;
    private final PlatformAccessService accessService;
    private final AuditQueryService auditQueryService;
    private final AuthService authService;

    public PlatformCenterController(PlatformCatalogService catalogService, PlatformAccessService accessService, AuditQueryService auditQueryService, AuthService authService) {
        this.catalogService = catalogService;
        this.accessService = accessService;
        this.auditQueryService = auditQueryService;
        this.authService = authService;
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/access/me")
    public ResponseEntity<ApiResponse<CurrentUserDto>> me(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.requirePlatformAdmin(request)));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/foundation/applications")
    public ApiResponse<CursorPageDto<Map<String, Object>>> applications(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.applications()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/foundation/snow-groups")
    public ApiResponse<CursorPageDto<Map<String, Object>>> snowGroups(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.snowGroups()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/foundation/workspaces")
    public ApiResponse<CursorPageDto<Map<String, Object>>> workspaces(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.workspaces()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/foundation/scope/resolve")
    public ApiResponse<Map<String, Object>> resolveScope(
            HttpServletRequest request,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String snowGroupId,
            @RequestParam(required = false) String projectId
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.resolveScope(workspaceId, applicationId, snowGroupId, projectId));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/templates")
    public ApiResponse<CursorPageDto<Map<String, Object>>> templates(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.templates()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/templates/{id}")
    public ApiResponse<Map<String, Object>> templateDetail(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.templateDetail(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/templates/{id}/versions")
    public ApiResponse<List<Map<String, Object>>> templateVersions(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.templateVersions(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/configurations")
    public ApiResponse<CursorPageDto<Map<String, Object>>> configurations(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.configurations()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/configurations/{id}")
    public ApiResponse<Map<String, Object>> configurationDetail(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.configurationDetail(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/audit")
    public ApiResponse<CursorPageDto<AuditRecordDto>> audit(
            HttpServletRequest request,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeId,
            @RequestParam(required = false) String timeRange
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(auditQueryService.list(category, actor, objectType, objectId, outcome, scopeType, scopeId, timeRange));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/audit/{id}")
    public ApiResponse<AuditRecordDto> auditDetail(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(auditQueryService.detail(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/access/users")
    public ApiResponse<CursorPageDto<PlatformUserDto>> users(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String profileSource,
            @RequestParam(required = false) String q
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(accessService.listUsers(status, profileSource, q)));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/access/users")
    public ResponseEntity<ApiResponse<PlatformUserDto>> createUser(HttpServletRequest servletRequest, @RequestBody UpsertPlatformUserRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(accessService.createUser(request, actor.staffId())));
    }

    @PutMapping(ApiConstants.API_V1 + "/platform/access/users/{staffId}")
    public ApiResponse<PlatformUserDto> updateUser(HttpServletRequest servletRequest, @PathVariable String staffId, @RequestBody UpsertPlatformUserRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ApiResponse.ok(accessService.updateUser(new UpsertPlatformUserRequest(
                staffId,
                request.displayName(),
                request.staffName(),
                request.avatarUrl(),
                request.email(),
                request.profileSource(),
                request.status()
        ), actor.staffId()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/access/assignments")
    public ApiResponse<CursorPageDto<RoleAssignmentDto>> assignments(
            HttpServletRequest request,
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeId
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(accessService.listAssignments(staffId, role, scopeType, scopeId)));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/access/assignments")
    public ResponseEntity<ApiResponse<RoleAssignmentDto>> assign(HttpServletRequest servletRequest, @RequestBody AssignRoleRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(accessService.assignRole(request, actor.staffId())));
    }

    @DeleteMapping(ApiConstants.API_V1 + "/platform/access/assignments/{id}")
    public ResponseEntity<Void> revoke(HttpServletRequest request, @PathVariable String id) {
        CurrentUserDto actor = authService.requirePlatformAdmin(request);
        accessService.revoke(id, actor.staffId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/policies")
    public ApiResponse<CursorPageDto<Map<String, Object>>> policies(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.policies()));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/exceptions")
    public ApiResponse<List<Map<String, Object>>> policyExceptions(HttpServletRequest request, @PathVariable String policyId) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.policyExceptions(policyId));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/integrations/adapters")
    public ApiResponse<List<Map<String, Object>>> adapters(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.adapters());
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/integrations/connections")
    public ApiResponse<CursorPageDto<Map<String, Object>>> connections(HttpServletRequest request) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(CursorPageDto.of(catalogService.connections()));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/integrations/connections/{id}/test")
    public ApiResponse<Map<String, Object>> testConnection(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(catalogService.testConnection(id));
    }
}
