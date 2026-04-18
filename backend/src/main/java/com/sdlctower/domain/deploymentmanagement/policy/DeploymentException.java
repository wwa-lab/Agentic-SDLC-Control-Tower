package com.sdlctower.domain.deploymentmanagement.policy;

public class DeploymentException extends RuntimeException {

    private final String errorCode;

    public DeploymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
