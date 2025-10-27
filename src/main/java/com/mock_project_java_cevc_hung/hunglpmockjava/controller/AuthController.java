package com.mock_project_java_cevc_hung.hunglpmockjava.controller;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.GoogleLoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.LoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.RegisterRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.JwtResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.EmailAlreadyExistsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.GoogleAuthenticationException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.InvalidCredentialsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", "Email or password is incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            JwtResponse response = authService.registerUser(registerRequest);
            return ResponseEntity.ok(response);
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already exists");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            JwtResponse response = authService.authenticateGoogleUser(googleLoginRequest);
            return ResponseEntity.ok(response);
        } catch (GoogleAuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Google authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
