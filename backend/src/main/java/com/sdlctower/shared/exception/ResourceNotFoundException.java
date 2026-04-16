package com.sdlctower.shared.exception;

/**
 * Thrown when a requested resource does not exist.
 * Handled by GlobalExceptionHandler to return 404 with ApiResponse envelope.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
