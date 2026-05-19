package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.UpdateVariantPriceRequest;
import com.nhhoang.synexbackend.dto.request.UpsertProductVariantRequest;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public List<ProductVariant> getByProductId(Long productId) {
        ensureProductExists(productId);
        return productVariantRepository.findByProductId(productId);
    }

    public ProductVariant create(Long productId, UpsertProductVariantRequest request) {
        validateUpsertRequest(request);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String normalizedSku = normalizeSku(request.getSku());
        if (productVariantRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new RuntimeException("SKU already exists");
        }

        ProductVariant variant = new ProductVariant();
        applyRequest(variant, request);
        variant.setSku(normalizedSku);
        variant.setProduct(product);

        return productVariantRepository.save(variant);
    }

    public ProductVariant update(Long productId, Long variantId, UpsertProductVariantRequest request) {
        validateUpsertRequest(request);

        ProductVariant variant = findVariant(productId, variantId);
        String normalizedSku = normalizeSku(request.getSku());

        if (productVariantRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, variantId)) {
            throw new RuntimeException("SKU already exists");
        }

        applyRequest(variant, request);
        variant.setSku(normalizedSku);

        return productVariantRepository.save(variant);
    }

    public ProductVariant updatePrice(Long productId, Long variantId, UpdateVariantPriceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Price request is required");
        }

        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Variant price must be greater than 0");
        }

        ProductVariant variant = findVariant(productId, variantId);
        variant.setPrice(request.getPrice());
        return productVariantRepository.save(variant);
    }

    public void delete(Long productId, Long variantId) {
        ProductVariant variant = findVariant(productId, variantId);
        productVariantRepository.delete(variant);
    }

    private ProductVariant findVariant(Long productId, Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to selected product");
        }

        return variant;
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
    }

    private void validateUpsertRequest(UpsertProductVariantRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Variant request is required");
        }

        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }

        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Variant price must be greater than 0");
        }

        if (request.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    private void applyRequest(ProductVariant variant, UpsertProductVariantRequest request) {
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        variant.setActive(request.isActive());
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }
}
