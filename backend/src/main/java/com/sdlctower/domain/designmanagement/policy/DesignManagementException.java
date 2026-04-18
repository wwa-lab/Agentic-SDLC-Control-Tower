package com.sdlctower.domain.designmanagement.policy;

import org.springframework.http.HttpStatus;

public class DesignManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public DesignManagementException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public String render() {
        return code + ": " + getMessage();
    }

    public static DesignManagementException forbidden(String code, String message) {
        return new DesignManagementException(code, HttpStatus.FORBIDDEN, message);
    }

    public static DesignManagementException notFound(String code, String message) {
        return new DesignManagementException(code, HttpStatus.NOT_FOUND, message);
    }

    public static DesignManagementException conflict(String code, String message) {
        return new DesignManagementException(code, HttpStatus.CONFLICT, message);
    }

    public static DesignManagementException invalid(String code, String message) {
        return new DesignManagementException(code, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static DesignManagementException badRequest(String code, String message) {
        return new DesignManagementException(code, HttpStatus.BAD_REQUEST, message);
    }
}
