package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private Long categoryId;
    private Long brandId;
    
    // Nhận nhiều ảnh cho gallery sản phẩm
    private List<MultipartFile> productImages;
    
    // Nhận danh sách biến thể
    private List<ProductVariantRequest> variants;
}