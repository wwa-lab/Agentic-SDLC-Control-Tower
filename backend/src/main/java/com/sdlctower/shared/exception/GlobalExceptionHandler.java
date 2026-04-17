package com.sdlctower.shared.exception;

import com.sdlctower.domain.designmanagement.policy.DesignManagementException;
import com.sdlctower.domain.projectspace.ProjectAccessDeniedException;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.teamspace.WorkspaceAccessDeniedException;
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
        return request != null && request.getRequestURI() != null && request.getRequestURI().contains("/design-management")
                ? "DM_VALIDATION_ERROR"
                : "PM_VALIDATION_ERROR";
    }
}
