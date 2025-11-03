package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ProfileUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.UserUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.UserResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiProfileResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.EmailAlreadyExistsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    public UserResponse getUserById(Long id) {
        UserEntity user = findUserById(id);
        return convertToUserResponse(user);
    }
    
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = findUserById(id);
        
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        user.setName(request.getName());
        user.setPhone_number(request.getPhone_number());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        UserEntity savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }
    
    public void deleteUser(Long id) {
        UserEntity user = findUserById(id);
        userRepository.delete(user);
    }
    
    private UserResponse convertToUserResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone_number(user.getPhone_number())
                .email(user.getEmail())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .role(user.getRole())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    public ApiProfileResponse getProfile(Long userId) {
        UserEntity user = findUserById(userId);
        return convertToResponse(user);
    }
    
    public ApiProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        UserEntity user = findUserById(userId);
        
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        user.setName(request.getName());
        user.setPhone_number(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        
        UserEntity savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }
    
    public void deleteProfile(Long userId) {
        UserEntity user = findUserById(userId);
        userRepository.delete(user);
    }
    
    private ApiProfileResponse convertToResponse(UserEntity user) {
        return ApiProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhone_number())
                .email(user.getEmail())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("profile.api.error.user.not_found", id)));
    }
}
