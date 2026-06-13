package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.CartItem;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.CartItemRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CartItem createCartItem(User user, ProductVariant variant) {
        CartItem newItem = new CartItem();
        newItem.setUser(user);
        newItem.setVariant(variant);
        newItem.setQuantity(0);
        return newItem;
    }

    @Transactional
    public CartItem addToCart(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User currentUser = getCurrentUser();
        ProductVariant variant = resolveVariant(variantId);
        CartItem item = cartItemRepository.findByUserIdAndVariantId(currentUser.getId(), variantId)
                .orElseGet(() -> createCartItem(currentUser, variant));

        int updatedQuantity = item.getQuantity() + quantity;
        if (variant.getStockQuantity() < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + variant.getProduct().getName());
        }

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    public List<CartItem> getCurrentUserCart() {
        return cartItemRepository.findAllByUserId(getCurrentUser().getId());
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public CartItem increaseItemQuantity(Long cartItemId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        ProductVariant variant = item.getVariant();
        int updatedQuantity = Math.min(variant.getStockQuantity(), item.getQuantity() + amount);

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem decreaseItemQuantity(Long cartItemId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        int updatedQuantity = item.getQuantity() - amount;

        if (updatedQuantity < 1) {
            removeFromCart(cartItemId);
            return null;
        }

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    private ProductVariant resolveVariant(Long variantId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID must not be null");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (!variant.isActive()) {
            throw new RuntimeException("Selected variant is inactive");
        }

        if (variant.getProduct() == null || !variant.getProduct().isActive()) {
            throw new RuntimeException("This product is no longer available for purchase");
        }

        return variant;
    }
}