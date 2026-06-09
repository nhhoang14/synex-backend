package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private MultipartFile imageFile; // Nhận file từ FE
    private String imageUrl; // Để giữ lại URL cũ nếu không upload ảnh mới
}