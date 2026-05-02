package com.sdlctower.shared.exception;

import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.domain.projectspace.ProjectAccessDeniedException;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.teamspace.WorkspaceAccessDeniedException;
import com.sdlctower.domain.codebuildmanagement.policy.CodeBuildManagementException;
import com.sdlctower.domain.deploymentmanagement.policy.DeploymentException;
import com.sdlctower.domain.testingmanagement.policy.TestingManagementException;
import com.sdlctower.platform.auth.PlatformAuthException;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler — all errors return a consistent ApiResponse envelope.
 * No stack traces are leaked to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        HttpStatus status = "LAST_PLATFORM_ADMIN".equals(ex.getMessage()) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(PlatformAuthException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlatformAuth(PlatformAuthException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(WorkspaceAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(WorkspaceAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectAccessDenied(ProjectAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(ProjectManagementException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectManagement(ProjectManagementException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(ApiResponse.fail(ex.render()));
    }

    @ExceptionHandler(DesignManagementException.class)
    public ResponseEntity<ApiResponse<Void>> handleDesignManagement(DesignManagementException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(ApiResponse.fail(ex.render()));
    }

    @ExceptionHandler(TestingManagementException.class)
    public ResponseEntity<ApiResponse<Void>> handleTestingManagement(TestingManagementException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(ApiResponse.fail(ex.render()));
    }

    @ExceptionHandler(CodeBuildManagementException.class)
    public ResponseEntity<ApiResponse<Void>> handleCodeBuildManagement(CodeBuildManagementException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(ApiResponse.fail(ex.render()));
    }

    @ExceptionHandler(DeploymentException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeployment(DeploymentException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "DP_WORKSPACE_FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "DP_ROLE_REQUIRED" -> HttpStatus.FORBIDDEN;
            case "DP_AI_AUTONOMY_INSUFFICIENT" -> HttpStatus.FORBIDDEN;
            case "DP_RATE_LIMITED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "DP_INGEST_SIGNATURE_INVALID" -> HttpStatus.UNAUTHORIZED;
            case "DP_JENKINS_INSTANCE_UNKNOWN" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getErrorCode() + ": " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail(validationPrefix(request) + ": " + message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(validationPrefix(request) + ": " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Internal server error"));
    }

    private String validationPrefix(HttpServletRequest request) {
        if (request != null && request.getRequestURI() != null) {
            String uri = request.getRequestURI();
            if (uri.contains("/design-management")) {
                return "DM_VALIDATION_ERROR";
            }
            if (uri.contains("/testing-management") || uri.contains("/testing")) {
                return "TM_VALIDATION_ERROR";
            }
            if (uri.contains("/code-build-management")) {
                return "CB_VALIDATION_ERROR";
            }
            if (uri.contains("/deployment-management")) {
                return "DP_VALIDATION_ERROR";
            }
        }
        return "PM_VALIDATION_ERROR";
    }
}
