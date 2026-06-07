package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.UpdateVariantPriceRequest;
import com.nhhoang.synexbackend.dto.request.UpsertProductVariantRequest;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.ProductVariantAttribute;
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
        
        // Gán thuộc tính cho tạo mới
        addAttributesToVariant(variant, request.getAttributes());

        return productVariantRepository.saveAndFlush(variant);
    }

    // TỐI ƯU BULK: Gom tất cả lại rồi saveAll 1 lần cuối, bỏ saveAndFlush từng vòng lặp
    public List<ProductVariant> createBulk(Long productId, List<UpsertProductVariantRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Variant list is empty");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variantsToSave = requests.stream().map(req -> {
            validateUpsertRequest(req);
            String normalizedSku = normalizeSku(req.getSku());
            if (productVariantRepository.existsBySkuIgnoreCase(normalizedSku)) {
                throw new RuntimeException("SKU " + normalizedSku + " already exists");
            }

            ProductVariant variant = new ProductVariant();
            applyRequest(variant, req);
            variant.setSku(normalizedSku);
            variant.setProduct(product);
            addAttributesToVariant(variant, req.getAttributes());
            return variant;
        }).toList();

        return productVariantRepository.saveAllAndFlush(variantsToSave);
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

        // Xử lý cập nhật attribute an toàn cho Hibernate
        if (request.getAttributes() != null) {
            variant.getAttributes().clear();
            addAttributesToVariant(variant, request.getAttributes());
        }

        return productVariantRepository.saveAndFlush(variant);
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
        if (productVariantRepository.findByProductId(productId).size() <= 1) {
            throw new RuntimeException("Each product must have at least one variant");
        }

        ProductVariant variant = findVariant(productId, variantId);
        productVariantRepository.delete(variant);
        productVariantRepository.flush();
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
        variant.setActive(request.isActive());

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            variant.setImageUrl(request.getImageUrl().trim());
        }
    }

    // Tách hàm xử lý mapping attribute riêng biệt để tái sử dụng sạch sẽ hơn
    private void addAttributesToVariant(ProductVariant variant, List<UpsertProductVariantRequest.VariantAttributeRequest> attributeRequests) {
        if (attributeRequests == null) return;
        
        for (UpsertProductVariantRequest.VariantAttributeRequest attrReq : attributeRequests) {
            ProductVariantAttribute attr = new ProductVariantAttribute();
            attr.setAttributeName(attrReq.getAttributeName());
            attr.setAttributeValue(attrReq.getAttributeValue());
            attr.setVariant(variant);
            variant.getAttributes().add(attr);
        }
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }
}