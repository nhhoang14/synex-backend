package com.nhhoang.synexbackend.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {
    private Long id;
    private String sku;
    private Double price;
    private Integer stock;
    private MultipartFile variantImage;
    private String imageUrl;
    private boolean active = true; 
    private List<VariantAttributeRequest> attributes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantAttributeRequest {
        private String attributeName;
        private String attributeValue;
    }
}