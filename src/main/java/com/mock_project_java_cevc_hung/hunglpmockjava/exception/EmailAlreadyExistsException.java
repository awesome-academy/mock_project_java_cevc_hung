package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Exception thrown when attempting to register with an email that already exists
 */
public class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' is already registered");
    }
}


