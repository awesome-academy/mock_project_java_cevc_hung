package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Pattern(regexp = "^0\\d{9,10}$", message = "Phone number must be 10 or 11 digits")
    private String phoneNumber;
    
    @Email(message = "Email invalid format")
    @NotBlank(message = "Email is required")
    private String email;
    
    private String address;
    
    private Boolean isActive;
    
    private UserEntity.Role role;
}


