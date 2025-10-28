package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Exception thrown when Google authentication fails
 */
public class GoogleAuthenticationException extends AuthException {
    public GoogleAuthenticationException(String message) {
        super("Google authentication failed: " + message);
    }
}


