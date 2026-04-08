package com.nhhoang.synexbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private String phone;
}
