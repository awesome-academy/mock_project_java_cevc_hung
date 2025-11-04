package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    
    private Long bookingId;
    private String paymentRef;
    private Double amount;
    private String status;
    private String message;
    private Long revenueId;
}

