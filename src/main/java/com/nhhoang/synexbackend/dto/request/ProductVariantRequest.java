package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductVariantRequest {
    private String sku;
    private Double price;
    private Integer stock;
    private MultipartFile variantImage; // Ảnh riêng cho từng biến thể (màu sắc, kích cỡ)
}