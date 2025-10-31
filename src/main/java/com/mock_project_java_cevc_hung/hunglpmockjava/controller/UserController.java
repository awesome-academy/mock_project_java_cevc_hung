package com.mock_project_java_cevc_hung.hunglpmockjava.controller;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        UserEntity user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName() != null ? user.getName() : "");
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("address", user.getAddress() != null ? user.getAddress() : "");
        response.put("phone_number", user.getPhone_number() != null ? user.getPhone_number() : "");
        response.put("role", user.getRole() != null ? user.getRole().name() : "");
        response.put("provider", user.getProvider() != null ? user.getProvider().name() : "");
        response.put("is_active", 1);
        response.put("created_at", user.getCreatedAt() != null ? user.getCreatedAt() : "");
        response.put("updated_at", user.getUpdatedAt() != null ? user.getUpdatedAt() : "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        UserEntity user = userOpt.get();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("user", Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "provider", user.getProvider().name()
        ));
        
        // Add user statistics
        dashboard.put("statistics", Map.of(
            "total_bookings", user.getBookings() != null ? user.getBookings().size() : 0,
            "total_reviews", user.getReviews() != null ? user.getReviews().size() : 0,
            "total_comments", user.getComments() != null ? user.getComments().size() : 0
        ));

        return ResponseEntity.ok(dashboard);
    }

    // FE clear jwt token cookie on logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
