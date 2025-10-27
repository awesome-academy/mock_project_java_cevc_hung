package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

/**
 * Exception thrown when JWT token validation fails
 */
public class JwtValidationException extends AuthException {
    private final JwtErrorType errorType;
    
    public JwtValidationException(JwtErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public JwtValidationException(JwtErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public JwtErrorType getErrorType() {
        return errorType;
    }
    
    public enum JwtErrorType {
        MALFORMED,
        EXPIRED,
        UNSUPPORTED,
        EMPTY_CLAIMS,
        INVALID_SIGNATURE
    }
}
