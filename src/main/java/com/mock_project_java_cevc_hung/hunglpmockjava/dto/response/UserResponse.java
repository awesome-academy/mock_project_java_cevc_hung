package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private Boolean isActive;
    private UserEntity.Role role;
    private UserEntity.Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


