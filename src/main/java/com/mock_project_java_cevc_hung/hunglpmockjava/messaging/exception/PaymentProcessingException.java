package com.mock_project_java_cevc_hung.hunglpmockjava.messaging.exception;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

