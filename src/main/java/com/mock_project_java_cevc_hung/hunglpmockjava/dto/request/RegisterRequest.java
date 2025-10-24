package com.mock_project_java_cevc_hung.hunglpmockjava.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String address;
    private String phoneNumber = null;
}
