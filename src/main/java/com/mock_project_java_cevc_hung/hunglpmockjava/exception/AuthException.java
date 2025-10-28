package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Base exception for authentication-related errors
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}


