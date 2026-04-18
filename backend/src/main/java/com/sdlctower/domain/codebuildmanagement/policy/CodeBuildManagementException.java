package com.sdlctower.domain.codebuildmanagement.policy;

import org.springframework.http.HttpStatus;

public class CodeBuildManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public CodeBuildManagementException(String code, HttpStatus status, String message) {
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

    public static CodeBuildManagementException forbidden(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.FORBIDDEN, message);
    }

    public static CodeBuildManagementException notFound(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.NOT_FOUND, message);
    }

    public static CodeBuildManagementException conflict(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.CONFLICT, message);
    }

    public static CodeBuildManagementException invalid(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static CodeBuildManagementException badRequest(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.BAD_REQUEST, message);
    }

    public static CodeBuildManagementException unauthorized(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.UNAUTHORIZED, message);
    }

    public static CodeBuildManagementException serviceUnavailable(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static CodeBuildManagementException tooManyRequests(String code, String message) {
        return new CodeBuildManagementException(code, HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
