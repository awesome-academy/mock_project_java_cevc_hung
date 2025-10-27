package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Exception thrown when login credentials are invalid
 */
public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}


