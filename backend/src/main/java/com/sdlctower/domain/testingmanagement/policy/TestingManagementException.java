package com.sdlctower.domain.testingmanagement.policy;

import org.springframework.http.HttpStatus;

public class TestingManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public TestingManagementException(String code, HttpStatus status, String message) {
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

    public static TestingManagementException forbidden(String code, String message) {
        return new TestingManagementException(code, HttpStatus.FORBIDDEN, message);
    }

    public static TestingManagementException notFound(String code, String message) {
        return new TestingManagementException(code, HttpStatus.NOT_FOUND, message);
    }

    public static TestingManagementException conflict(String code, String message) {
        return new TestingManagementException(code, HttpStatus.CONFLICT, message);
    }

    public static TestingManagementException invalid(String code, String message) {
        return new TestingManagementException(code, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static TestingManagementException badRequest(String code, String message) {
        return new TestingManagementException(code, HttpStatus.BAD_REQUEST, message);
    }
}
