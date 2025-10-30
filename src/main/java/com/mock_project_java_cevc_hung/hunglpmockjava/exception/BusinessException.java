package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Exception thrown for business logic violations
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

