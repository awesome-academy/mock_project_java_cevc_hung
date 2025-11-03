package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ProfileUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiProfileResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.UserDetailsImpl;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        ApiProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        ApiProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        userService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}

