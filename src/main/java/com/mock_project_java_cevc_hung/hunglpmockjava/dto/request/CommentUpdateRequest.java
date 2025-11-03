package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
}

