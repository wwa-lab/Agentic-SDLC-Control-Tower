package com.sdlctower.domain.projectmanagement.policy;

import org.springframework.http.HttpStatus;

public class ProjectManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ProjectManagementException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String render() {
        return code + ": " + getMessage();
    }

    public static ProjectManagementException forbidden(String code, String message) {
        return new ProjectManagementException(code, HttpStatus.FORBIDDEN, message);
    }

    public static ProjectManagementException notFound(String code, String message) {
        return new ProjectManagementException(code, HttpStatus.NOT_FOUND, message);
    }

    public static ProjectManagementException conflict(String code, String message) {
        return new ProjectManagementException(code, HttpStatus.CONFLICT, message);
    }

    public static ProjectManagementException invalid(String code, String message) {
        return new ProjectManagementException(code, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static ProjectManagementException badRequest(String code, String message) {
        return new ProjectManagementException(code, HttpStatus.BAD_REQUEST, message);
    }
}
