package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ProductRequest {
    private Long id;
    private String name;
    private String description;
    private Long categoryId; 
    private Long brandId; 
    private boolean active;
    
    private List<MultipartFile> productImages;

    private List<String> keepImageUrls;
    
    private List<ProductVariantRequest> variants;
}