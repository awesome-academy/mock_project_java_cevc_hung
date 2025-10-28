package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourResponse {
    
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String location;
    private String thumbnailUrl;
    private Integer seatsTotal;
    private Integer seatsAvailable;
    private Double ratingAvg;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TourEntity.Status status;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Long totalBookings;
    private Long totalReviews;
}
