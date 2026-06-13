package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.dto.request.ProductRequest;
import com.nhhoang.synexbackend.dto.request.ProductVariantRequest;
import com.nhhoang.synexbackend.entity.Brand;
import com.nhhoang.synexbackend.entity.Category;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductImage;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.ProductVariantAttribute;
import com.nhhoang.synexbackend.repository.BrandRepository;
import com.nhhoang.synexbackend.repository.CategoryRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService { 

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository; 
    private final CategoryRepository categoryRepository; 
    private final BrandRepository brandRepository;   
    private final LocalImageUploadService fileService; 

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getActiveProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id);
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        validateProductRequest(request);

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setActive(true);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        product.setCategory(category);
        product.setBrand(brand);

        // xử lý Product Images
        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            List<ProductImage> images = request.getProductImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        String imageUrl = fileService.upload(file);
                        ProductImage image = new ProductImage();
                        image.setImageUrl(imageUrl);
                        image.setProduct(product); 
                        return image;
                    }).toList();
            product.getImages().addAll(images); 
        }

        // xử lý Product Variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = request.getVariants().stream()
                    .map(variantRequest -> {
                        validateProductVariantRequest(variantRequest);
                        String normalizedSku = normalizeSku(variantRequest.getSku());
                        if (productVariantRepository.existsBySkuIgnoreCase(normalizedSku)) {
                            throw new RuntimeException("SKU " + normalizedSku + " already exists");
                        }

                        ProductVariant variant = new ProductVariant();
                        applyVariantRequest(variant, variantRequest);
                        variant.setSku(normalizedSku);
                        variant.setProduct(product); 

                        addAttributesToVariant(variant, variantRequest.getAttributes());
                        return variant;
                    }).toList();
            product.getVariants().addAll(variants);
        } else {
            throw new IllegalArgumentException("At least one product variant is required");
        }

        return productRepository.saveAndFlush(product);
    }

    @Transactional 
    public Product updateProduct(Long id, ProductRequest request) {
        validateProductRequest(request);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found")); 

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setActive(request.isActive());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        existing.setCategory(category);
        existing.setBrand(brand);

        // xử lý Product Images
        List<String> urlsToKeep = request.getKeepImageUrls() != null ? request.getKeepImageUrls() : new java.util.ArrayList<>();

        if (existing.getImages() != null) {
            existing.getImages().removeIf(img -> {
                boolean shouldDelete = !urlsToKeep.contains(img.getImageUrl());
                if (shouldDelete) {
                    fileService.delete(img.getImageUrl());
                }
                return shouldDelete;
            });
        }

        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            for (MultipartFile file : request.getProductImages()) {
                if (file != null && !file.isEmpty()) {
                    String newImageUrl = fileService.upload(file);
                    ProductImage newImage = new ProductImage();
                    newImage.setImageUrl(newImageUrl);
                    newImage.setProduct(existing); 
                    existing.getImages().add(newImage);
                }
            }
        }

        // xử lý Product Variants
        Map<Long, ProductVariant> existingVariantsMap = existing.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

        List<ProductVariant> updatedVariants = new java.util.ArrayList<>();
        for (ProductVariantRequest variantRequest : request.getVariants()) {
            validateProductVariantRequest(variantRequest);
            String normalizedSku = normalizeSku(variantRequest.getSku());

            ProductVariant variant;
            if (variantRequest.getId() != null && existingVariantsMap.containsKey(variantRequest.getId())) {
                variant = existingVariantsMap.remove(variantRequest.getId()); 
                if (productVariantRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, variant.getId())) {
                    throw new RuntimeException("SKU " + normalizedSku + " already exists for another variant");
                }
            } else {
                variant = new ProductVariant();
                if (productVariantRepository.existsBySkuIgnoreCase(normalizedSku)) {
                    throw new RuntimeException("SKU " + normalizedSku + " already exists");
                }
            }

            applyVariantRequest(variant, variantRequest);
            variant.setSku(normalizedSku);
            variant.setProduct(existing);
            
            if (variant.getAttributes() != null) {
                variant.getAttributes().clear();
            }
            addAttributesToVariant(variant, variantRequest.getAttributes());
            updatedVariants.add(variant);
        }

        existing.getVariants().clear(); 
        existing.getVariants().addAll(updatedVariants); 

        return productRepository.saveAndFlush(existing);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (existing.getVariants() != null && !existing.getVariants().isEmpty()) {
            for (ProductVariant variant : existing.getVariants()) {
                if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
                    fileService.delete(variant.getImageUrl()); 
                }
            }
        }

        if (existing.getImages() != null && !existing.getImages().isEmpty()) {
            for (ProductImage img : existing.getImages()) {
                if (img.getImageUrl() != null && !img.getImageUrl().isBlank()) {
                    fileService.delete(img.getImageUrl()); 
                }
            }
        }

        productRepository.delete(existing);
        productRepository.flush();
    }

    private void validateProductRequest(ProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Product request is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Product description is required");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Product category ID is required");
        }
        if (request.getBrandId() == null) {
            throw new IllegalArgumentException("Product brand ID is required");
        }
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("At least one product variant is required");
        }
    }

    private void validateProductVariantRequest(ProductVariantRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Variant request is required");
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("Variant price must be greater than 0");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    private void applyVariantRequest(ProductVariant variant, ProductVariantRequest request) {
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStock());
        variant.setActive(request.isActive());

        if (request.getVariantImage() != null && !request.getVariantImage().isEmpty()) {
            if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
                fileService.delete(variant.getImageUrl());
            }
            variant.setImageUrl(fileService.upload(request.getVariantImage()));
        } else if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            variant.setImageUrl(request.getImageUrl()); 
        }
    }

    private void addAttributesToVariant(ProductVariant variant, List<ProductVariantRequest.VariantAttributeRequest> attributeRequests) {
        if (attributeRequests == null) return;
        for (ProductVariantRequest.VariantAttributeRequest attrReq : attributeRequests) {
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