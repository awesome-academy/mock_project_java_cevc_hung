package com.mock_project_java_cevc_hung.hunglpmockjava.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle ResourceNotFoundException - 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "Resource not found",
                    ex.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // For MVC requests, log and let controller handle or redirect
        logger.info("MVC request - Resource not found, redirect will be handled by controller");
        return handleMvcException(ex, request, "Resource not found: " + ex.getMessage());
    }

    /**
     * Handle BusinessException - 400 Bad Request
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        logger.warn("Business exception: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Business rule violation",
                    ex.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        return handleMvcException(ex, request, ex.getMessage());
    }

    /**
     * Handle AuthException - 401 Unauthorized
     */
    @ExceptionHandler(AuthException.class)
    public Object handleAuthException(
            AuthException ex,
            HttpServletRequest request) {
        
        logger.warn("Authentication exception: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed",
                    ex.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        return handleMvcException(ex, request, ex.getMessage());
    }

    /**
     * Handle RuntimeException - Generic runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        
        if (isApiRequest(request)) {
            // Check if it's a not found scenario
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                Map<String, Object> errorResponse = createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        "Resource not found",
                        ex.getMessage(),
                        request.getRequestURI()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Request failed",
                    ex.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        return handleMvcException(ex, request, ex.getMessage());
    }

    /**
     * Handle NoResourceFoundException - Static resources not found (font files, etc.)
     * This is not a critical error, just log as debug/warn
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        
        String path = request.getRequestURI();
        if (path.contains("/vendor/") || path.contains("/fonts/") || path.contains("/webfonts/")) {
            logger.debug("Static resource not found: {}", path);
        } else {
            logger.warn("Resource not found: {}", path);
        }
        
        if (isApiRequest(request)) {
            return ResponseEntity.notFound().build();
        }
        
        return null;
    }

    /**
     * Handle all other exceptions - 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error",
                    "An unexpected error occurred",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        
        return handleMvcException(ex, request, "An unexpected error occurred. Please try again.");
    }

    /**
     * Check if request is an API request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    /**
     * Create standardized error response for API
     */
    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        
        return errorResponse;
    }

    /**
     * Handle exceptions for MVC requests (redirect to appropriate page)
     * Note: Flash attributes cannot be set directly from @ControllerAdvice
     * Controllers should still handle their own try-catch for flash attributes
     */
    private ModelAndView handleMvcException(
            Exception ex,
            HttpServletRequest request,
            String userMessage) {
        
        String redirectUrl = getDefaultRedirectUrl(request);
        ModelAndView modelAndView = new ModelAndView("redirect:" + redirectUrl);
        modelAndView.addObject("error", userMessage);
        
        logger.error("MVC exception - redirecting to: {}, error: {}", redirectUrl, userMessage);
        
        return modelAndView;
    }

    /**
     * Get default redirect URL based on request path
     */
    private String getDefaultRedirectUrl(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        if (path.contains("/categories")) {
            return "/admin/categories";
        } else if (path.contains("/tours")) {
            return "/admin/tours";
        } else if (path.contains("/admin")) {
            return "/admin/dashboard";
        }
        
        return "/";
    }
}

