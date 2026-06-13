package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ContactMessageRequest {
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private MultipartFile imageFile;
    private String imageUrl;
}