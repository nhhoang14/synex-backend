package com.nhhoang.synexbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertProductVariantRequest {
    private String sku;
    private double price;
    private int stockQuantity;
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
