package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.GoogleLoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.LoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.RegisterRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.JwtResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    GoogleOAuth2Service googleOAuth2Service;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserEntity user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        
        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .build();
    }

    public JwtResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        UserEntity user = UserEntity.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(encoder.encode(registerRequest.getPassword()))
                .address(registerRequest.getAddress())
                .phone_number(registerRequest.getPhoneNumber())
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .build();
    }

    public JwtResponse authenticateGoogleUser(GoogleLoginRequest googleLoginRequest) {
        // Validate Google ID Token
        GoogleIdToken idToken = googleOAuth2Service.verifyToken(googleLoginRequest.getIdToken());
        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID token");
        }

        // Get user info from verified token
        String email = googleOAuth2Service.getEmailFromToken(idToken);
        String name = googleOAuth2Service.getNameFromToken(idToken);
        
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email not found in Google token");
        }

        // Check if user exists
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // Create new user with verified Google info
            user = UserEntity.builder()
                    .name(name != null ? name : "Google User")
                    .email(email)
                    .password(encoder.encode("google_oauth2_user"))
                    .phone_number("0000000000")
                    .address("Google User Address")
                    .provider(UserEntity.Provider.GOOGLE)
                    .build();
            userRepository.save(user);
        }

        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .build();
    }
}
