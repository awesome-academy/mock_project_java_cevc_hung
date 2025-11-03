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
public class ApiProfileResponse {
    
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private Boolean isActive;
    private String role;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

