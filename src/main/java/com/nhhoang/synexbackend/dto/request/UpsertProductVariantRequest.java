package com.nhhoang.synexbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertProductVariantRequest {
    private String sku;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active = true;
}
