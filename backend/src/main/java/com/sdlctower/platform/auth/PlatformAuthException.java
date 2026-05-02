package com.sdlctower.platform.auth;

import org.springframework.http.HttpStatus;

public class PlatformAuthException extends RuntimeException {

    private final HttpStatus status;

    public PlatformAuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
