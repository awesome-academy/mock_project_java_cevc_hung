package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevenueResponse {
    
    private Long id;
    private LocalDate date;
    private Double totalRevenue;
    private Double tourRevenue;
    private Integer totalBookings;
    private Long bookingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

