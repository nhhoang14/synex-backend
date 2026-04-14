package com.nhhoang.synexbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageRequest {
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
}
