package com.sdlctower.platform.configuration;

import org.springframework.http.HttpStatus;

public class PlatformConfigurationException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public PlatformConfigurationException(String code, HttpStatus status) {
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
