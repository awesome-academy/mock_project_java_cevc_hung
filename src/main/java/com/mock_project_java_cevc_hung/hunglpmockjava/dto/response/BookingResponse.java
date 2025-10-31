package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
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
