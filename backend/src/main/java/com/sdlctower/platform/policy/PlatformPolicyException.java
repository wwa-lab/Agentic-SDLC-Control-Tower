package com.sdlctower.platform.policy;

import org.springframework.http.HttpStatus;

public class PlatformPolicyException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public PlatformPolicyException(String code, HttpStatus status) {
        super(code);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}
