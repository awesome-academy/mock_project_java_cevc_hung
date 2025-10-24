package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoogleLoginRequest {
    private String idToken; // Google ID Token (not access token)
    private String email;   // Optional: for validation
    private String name;    // Optional: for validation
}
