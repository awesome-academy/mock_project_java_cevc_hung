package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.LoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.RegisterRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.GoogleLoginRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.JwtResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.EmailAlreadyExistsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.GoogleAuthenticationException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.InvalidCredentialsException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private GoogleOAuth2Service googleOAuth2Service;

    @InjectMocks
    private AuthService authService;

    private UserEntity testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
            .email("test@example.com")
            .password("encodedPassword")
            .name("Test User")
            .role(UserEntity.Role.USER)
            .provider(UserEntity.Provider.LOCAL)
            .build();
        testUser.setId(1L);

        registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should register user successfully")
    void registerUser_ValidRequest_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwt-token");

        // When
        JwtResponse result = authService.registerUser(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(encoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_DuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void authenticateUser_ValidCredentials_ShouldReturnJwtResponse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class)))
                .thenReturn("jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When
        JwtResponse result = authService.authenticateUser(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should throw exception when credentials are invalid")
    void authenticateUser_InvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Should authenticate Google user and create new account")
    void authenticateGoogleUser_NewUser_ShouldCreateAndReturnToken() {
        // Given
        GoogleLoginRequest googleRequest = new GoogleLoginRequest();
        googleRequest.setIdToken("google-id-token");

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(googleOAuth2Service.verifyToken(anyString())).thenReturn(idToken);
        when(googleOAuth2Service.getEmailFromToken(any())).thenReturn("google@example.com");
        when(googleOAuth2Service.getNameFromToken(any())).thenReturn("Google User");
        when(googleOAuth2Service.getPhoneFromToken(any())).thenReturn("0123456789");
        when(googleOAuth2Service.getAddressFromToken(any())).thenReturn("Google Address");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        when(jwtUtils.generateTokenFromUsername(anyString())).thenReturn("jwt-token");

        // When
        JwtResponse result = authService.authenticateGoogleUser(googleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should authenticate existing Google user")
    void authenticateGoogleUser_ExistingUser_ShouldReturnToken() {
        // Given
        GoogleLoginRequest googleRequest = new GoogleLoginRequest();
        googleRequest.setIdToken("google-id-token");

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(googleOAuth2Service.verifyToken(anyString())).thenReturn(idToken);
        when(googleOAuth2Service.getEmailFromToken(any())).thenReturn("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateTokenFromUsername(anyString())).thenReturn("jwt-token");

        // When
        JwtResponse result = authService.authenticateGoogleUser(googleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository, never()).save(any()); // Should not create new user
    }

    @Test
    @DisplayName("Should throw exception when Google token is invalid")
    void authenticateGoogleUser_InvalidToken_ShouldThrowException() {
        // Given
        GoogleLoginRequest googleRequest = new GoogleLoginRequest();
        googleRequest.setIdToken("invalid-token");

        when(googleOAuth2Service.verifyToken(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.authenticateGoogleUser(googleRequest))
                .isInstanceOf(GoogleAuthenticationException.class);
    }

    @Test
    @DisplayName("Should throw exception when Google token has no email")
    void authenticateGoogleUser_NoEmail_ShouldThrowException() {
        // Given
        GoogleLoginRequest googleRequest = new GoogleLoginRequest();
        googleRequest.setIdToken("google-id-token");

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(googleOAuth2Service.verifyToken(anyString())).thenReturn(idToken);
        when(googleOAuth2Service.getEmailFromToken(any())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.authenticateGoogleUser(googleRequest))
                .isInstanceOf(GoogleAuthenticationException.class)
                .hasMessageContaining("Email not found");
    }
}
