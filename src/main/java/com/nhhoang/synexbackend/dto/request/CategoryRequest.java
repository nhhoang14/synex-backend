package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private MultipartFile imageFile;
    private String imageUrl;
}