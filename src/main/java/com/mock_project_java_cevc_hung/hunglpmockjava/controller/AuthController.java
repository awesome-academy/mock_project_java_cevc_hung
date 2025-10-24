package com.mock_project_java_cevc_hung.hunglpmockjava.controller;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.GoogleLoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.LoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.RegisterRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.JwtResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            JwtResponse response = authService.registerUser(registerRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            JwtResponse response = authService.authenticateGoogleUser(googleLoginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
