package com.nhhoang.synexbackend.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private String email;
    private String fullName;
    private String phone;
}
