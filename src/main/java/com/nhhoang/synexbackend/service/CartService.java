package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.CartItem;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.CartItemRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItem addToCart(Long productId, Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User currentUser = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = resolveVariant(product, variantId);

        CartItem item = findCartItem(currentUser.getId(), productId, variantId)
                .orElseGet(() -> createCartItem(currentUser.getId(), product, variant));

        int updatedQuantity = item.getQuantity() + quantity;
        if (getAvailableStock(variant) < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(updatedQuantity);

        return cartItemRepository.save(item);
    }

    public List<CartItem> getCurrentUserCart() {
        return cartItemRepository.findAllByUserId(getCurrentUser().getId());
    }

    @Transactional
    public void removeFromCart(Long productId, Long variantId) {
        Long userId = getCurrentUser().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ProductVariant variant = resolveVariant(product, variantId);
        if (variantId != null) {
            cartItemRepository.deleteByUserIdAndProductIdAndVariantId(userId, productId, variant.getId());
            return;
        }

        cartItemRepository.deleteByUserIdAndProductIdAndVariantId(userId, productId, variant.getId());
    }

    @Transactional
    public CartItem increaseItemQuantity(Long productId, Long variantId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = getCurrentUserCartItem(productId, variantId);
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        int updatedQuantity = item.getQuantity() + amount;

        if (getAvailableStock(variant) < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem decreaseItemQuantity(Long productId, Long variantId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = getCurrentUserCartItem(productId, variantId);
        int updatedQuantity = item.getQuantity() - amount;

        if (updatedQuantity < 1) {
            throw new IllegalArgumentException("Quantity cannot be less than 1. Use delete API to remove item");
        }

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    private CartItem getCurrentUserCartItem(Long productId, Long variantId) {
        Long userId = getCurrentUser().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ProductVariant variant = resolveVariant(product, variantId);
        return findCartItem(userId, productId, variant.getId())
                .orElseThrow(() -> new RuntimeException("Product is not in cart"));
    }

    private Optional<CartItem> findCartItem(Long userId, Long productId, Long variantId) {
        return cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId);
    }

    private CartItem createCartItem(Long userId, Product product, ProductVariant variant) {
        CartItem newItem = new CartItem();
        newItem.setUserId(userId);
        newItem.setProduct(product);
        newItem.setVariant(variant);
        newItem.setQuantity(0);
        return newItem;
    }

    private ProductVariant resolveVariant(Product product, Long variantId) {
        if (variantId == null) {
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            if (variants.size() == 1) {
                ProductVariant onlyVariant = variants.get(0);
                if (!onlyVariant.isActive()) {
                    throw new RuntimeException("Selected variant is inactive");
                }
                return onlyVariant;
            }

            throw new RuntimeException("Please select a variant for this product");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Variant does not belong to selected product");
        }

        if (!variant.isActive()) {
            throw new RuntimeException("Selected variant is inactive");
        }

        return variant;
    }

    private int getAvailableStock(ProductVariant variant) {
        return variant.getStockQuantity();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}