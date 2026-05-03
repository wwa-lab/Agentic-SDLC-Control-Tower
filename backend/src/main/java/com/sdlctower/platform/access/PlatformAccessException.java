package com.sdlctower.platform.access;

import org.springframework.http.HttpStatus;

public class PlatformAccessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public PlatformAccessException(String code, HttpStatus status) {
        super(code);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
