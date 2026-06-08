package com.nhhoang.synexbackend.service.impl;

import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductImage;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.ProductVariantAttribute;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import com.nhhoang.synexbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getActiveProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public Product createProduct(Product product) {
        syncAssociations(product);
        Product saved = productRepository.saveAndFlush(product);
        ensureDefaultVariant(saved);
        return saved;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        existing.setActive(product.isActive());
        existing.setImages(product.getImages());
        existing.setVariants(product.getVariants());

        syncAssociations(existing);

        Product saved = productRepository.saveAndFlush(existing);
        ensureDefaultVariant(saved);

        return saved;
    }

    @Override
    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(existing);
        productRepository.flush();
    }

    private void ensureDefaultVariant(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        if (variants != null && !variants.isEmpty()) {
            return;
        }

        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setSku(("DEFAULT-" + product.getId()).toUpperCase());
        defaultVariant.setPrice(0.0);
        defaultVariant.setStockQuantity(0);
        defaultVariant.setActive(true);
        defaultVariant.setImageUrl(null);
        defaultVariant.setProduct(product);

        productVariantRepository.saveAndFlush(defaultVariant);
    }

    private void syncAssociations(Product product) {
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                image.setProduct(product);
            }
        }

        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(product);
                if (variant.getAttributes() != null) {
                    for (ProductVariantAttribute attribute : variant.getAttributes()) {
                        attribute.setVariant(variant);
                    }
                }
            }
        }
    }
}
