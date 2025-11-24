package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourImportRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String thumbnailUrl;
    
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer seatsTotal;
    
    @Min(value = 0, message = "Available seats must be greater than or equal to 0")
    private Integer seatsAvailable;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String status;
    
    @NotBlank(message = "Category name is required")
    private String categoryName;
    
    private Double ratingAvg;
}
