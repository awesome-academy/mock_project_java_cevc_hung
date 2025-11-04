package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCommentResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private Long reviewId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

