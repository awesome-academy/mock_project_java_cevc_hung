package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourUpdateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer seatsTotal;
    
    @Min(value = 0, message = "Available seats must be greater than or equal to 0")
    private Integer seatsAvailable;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Long categoryId;
}
