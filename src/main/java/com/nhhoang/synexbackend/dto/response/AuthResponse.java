package com.nhhoang.synexbackend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String token;
    private String refreshToken;
}
