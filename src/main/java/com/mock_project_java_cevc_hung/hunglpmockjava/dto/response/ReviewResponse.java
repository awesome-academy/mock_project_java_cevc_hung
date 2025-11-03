package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long tourId;
    private String tourTitle;
    private Integer rating;
    private String content;
    private ReviewEntity.Status status;
    private ReviewEntity.Type type;
    private Long totalLikes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

