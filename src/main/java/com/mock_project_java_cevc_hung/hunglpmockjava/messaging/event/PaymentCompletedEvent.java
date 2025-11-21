package com.mock_project_java_cevc_hung.hunglpmockjava.messaging.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentCompletedEvent implements Serializable {
    private Long bookingId;
    private Double amount;
    private String paymentRef;
    private LocalDateTime paidAt;

    @Builder
    public PaymentCompletedEvent(Long bookingId, Double amount, String paymentRef, LocalDateTime paidAt) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentRef = paymentRef;
        this.paidAt = paidAt;
    }
}

