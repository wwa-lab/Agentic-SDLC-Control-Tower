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
import com.sdlctower.platform.configuration.ConfigurationDetailDto;
import com.sdlctower.platform.configuration.ConfigurationSummaryDto;
import com.sdlctower.platform.configuration.PlatformConfigurationService;
import com.sdlctower.platform.configuration.UpsertConfigurationRequest;
import com.sdlctower.platform.policy.CreatePolicyExceptionRequest;
import com.sdlctower.platform.policy.PlatformPolicyService;
import com.sdlctower.platform.policy.PolicyDto;
import com.sdlctower.platform.policy.PolicyExceptionDto;
import com.sdlctower.platform.policy.UpsertPolicyRequest;
import com.sdlctower.platform.shared.CursorPageDto;
import com.sdlctower.platform.template.PlatformTemplateService;
import com.sdlctower.platform.template.TemplateDetailDto;
import com.sdlctower.platform.template.TemplateSummaryDto;
import com.sdlctower.platform.template.TemplateVersionDto;
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
    private final PlatformTemplateService templateService;
    private final PlatformConfigurationService configurationService;
    private final PlatformPolicyService policyService;
    private final AuthService authService;

    public PlatformCenterController(PlatformCatalogService catalogService, PlatformAccessService accessService, AuditQueryService auditQueryService, PlatformTemplateService templateService, PlatformConfigurationService configurationService, PlatformPolicyService policyService, AuthService authService) {
        this.catalogService = catalogService;
        this.accessService = accessService;
        this.auditQueryService = auditQueryService;
        this.templateService = templateService;
        this.configurationService = configurationService;
        this.policyService = policyService;
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
    public ApiResponse<CursorPageDto<TemplateSummaryDto>> templates(
            HttpServletRequest request,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(templateService.list(kind, status, q));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/templates/{id}")
    public ApiResponse<TemplateDetailDto> templateDetail(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(templateService.detail(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/templates/{id}/versions")
    public ApiResponse<List<TemplateVersionDto>> templateVersions(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(templateService.versions(id));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/configurations")
    public ApiResponse<CursorPageDto<ConfigurationSummaryDto>> configurations(
            HttpServletRequest request,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(configurationService.list(kind, scopeType, scopeId, status, q));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/configurations/{id}")
    public ApiResponse<ConfigurationDetailDto> configurationDetail(HttpServletRequest request, @PathVariable String id) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(configurationService.detail(id));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/configurations")
    public ResponseEntity<ApiResponse<ConfigurationDetailDto>> createConfiguration(HttpServletRequest servletRequest, @RequestBody UpsertConfigurationRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(configurationService.create(request, actor.staffId())));
    }

    @PutMapping(ApiConstants.API_V1 + "/platform/configurations/{id}")
    public ApiResponse<ConfigurationDetailDto> updateConfiguration(HttpServletRequest servletRequest, @PathVariable String id, @RequestBody UpsertConfigurationRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ApiResponse.ok(configurationService.update(id, request, actor.staffId()));
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
    public ApiResponse<CursorPageDto<PolicyDto>> policies(
            HttpServletRequest request,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeId,
            @RequestParam(required = false) String boundTo,
            @RequestParam(required = false) String q
    ) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(policyService.list(category, status, scopeType, scopeId, boundTo, q));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}")
    public ApiResponse<PolicyDto> policyDetail(HttpServletRequest request, @PathVariable String policyId) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(policyService.detail(policyId));
    }

    @GetMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/exceptions")
    public ApiResponse<List<PolicyExceptionDto>> policyExceptions(HttpServletRequest request, @PathVariable String policyId) {
        authService.requirePlatformAdmin(request);
        return ApiResponse.ok(policyService.exceptions(policyId));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/policies")
    public ResponseEntity<ApiResponse<PolicyDto>> createPolicy(HttpServletRequest servletRequest, @RequestBody UpsertPolicyRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(policyService.create(request, actor.staffId())));
    }

    @PutMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}")
    public ApiResponse<PolicyDto> updatePolicy(HttpServletRequest servletRequest, @PathVariable String policyId, @RequestBody UpsertPolicyRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ApiResponse.ok(policyService.update(policyId, request, actor.staffId()));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/activate")
    public ApiResponse<PolicyDto> activatePolicy(HttpServletRequest servletRequest, @PathVariable String policyId) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ApiResponse.ok(policyService.activate(policyId, actor.staffId()));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/deactivate")
    public ApiResponse<PolicyDto> deactivatePolicy(HttpServletRequest servletRequest, @PathVariable String policyId) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ApiResponse.ok(policyService.deactivate(policyId, actor.staffId()));
    }

    @PostMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/exceptions")
    public ResponseEntity<ApiResponse<PolicyExceptionDto>> addPolicyException(HttpServletRequest servletRequest, @PathVariable String policyId, @RequestBody CreatePolicyExceptionRequest request) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(policyService.addException(policyId, request, actor.staffId())));
    }

    @DeleteMapping(ApiConstants.API_V1 + "/platform/policies/{policyId}/exceptions/{exceptionId}")
    public ResponseEntity<Void> revokePolicyException(HttpServletRequest servletRequest, @PathVariable String policyId, @PathVariable String exceptionId) {
        CurrentUserDto actor = authService.requirePlatformAdmin(servletRequest);
        policyService.revokeException(policyId, exceptionId, actor.staffId());
        return ResponseEntity.noContent().build();
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
