package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    
    @NotNull(message = "Tour ID is required")
    private Long tourId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 0, message = "Rating must be between 0 and 5")
    @Max(value = 5, message = "Rating must be between 0 and 5")
    private Integer rating;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Type is required")
    private ReviewEntity.Type type;
}

