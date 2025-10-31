package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiBookingResponse {
    
    private Long id;
    private Long tourId;
    private String tourTitle;
    private String tourLocation;
    private Integer qty;
    private Double amount;
    private BookingEntity.Status status;
    private String paymentRef;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

