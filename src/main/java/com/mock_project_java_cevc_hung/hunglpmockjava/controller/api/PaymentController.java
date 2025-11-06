package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.PaymentRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.PaymentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.PaymentService;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request, BindingResult bindingResult) {
        
        // Validate request
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed");
            errorResponse.put("message", "Invalid payment request");
            
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            errorResponse.put("errors", errors);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (BusinessException e) {
            logger.warn("Payment business error: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Payment processing failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

