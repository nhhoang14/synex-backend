package com.nhhoang.synexbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
}
