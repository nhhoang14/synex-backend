package com.nhhoang.synexbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String token;
    private String refreshToken;
}
