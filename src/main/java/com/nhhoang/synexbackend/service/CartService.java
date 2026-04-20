package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.entity.Cart;
import com.nhhoang.synexbackend.entity.CartItem;
import com.nhhoang.synexbackend.entity.Product;
import com.nhhoang.synexbackend.entity.ProductVariant;
import com.nhhoang.synexbackend.entity.User;
import com.nhhoang.synexbackend.repository.CartItemRepository;
import com.nhhoang.synexbackend.repository.CartRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.ProductVariantRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItem addToCart(Long cartId, Long productId, Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = resolveCart(cartId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = resolveVariant(product, variantId);

        CartItem item = findCartItem(cart.getId(), productId, variantId)
                .orElseGet(() -> createCartItem(cart, product, variant));

        int updatedQuantity = item.getQuantity() + quantity;
        if (getAvailableStock(product, variant) < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(updatedQuantity);

        return cartItemRepository.save(item);
    }

    public Cart getCurrentUserCart() {
        return resolveCurrentUserCart();
    }

    public Cart getCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        User currentUser = getCurrentUser();
        if (cart.getUser() != null && !cart.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized cart access");
        }

        return cart;
    }

    public Cart createCart() {
        User currentUser = getCurrentUser();
        return getOrCreateCurrentUserCart(currentUser);
    }

    @Transactional
    public void removeFromCart(Long productId, Long variantId) {
        Cart cart = getCurrentUserCartOrThrow();
        if (variantId != null) {
            cartItemRepository.deleteByCartIdAndProductIdAndVariantId(cart.getId(), productId, variantId);
            return;
        }

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
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

        if (getAvailableStock(product, variant) < updatedQuantity) {
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

    private Cart resolveCart(Long cartId) {
        if (cartId != null) {
            return getCart(cartId);
        }

        return resolveCurrentUserCart();
    }

    private Cart resolveCurrentUserCart() {
        User currentUser = getCurrentUser();
        return getOrCreateCurrentUserCart(currentUser);
    }

    private Cart getCurrentUserCartOrThrow() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId());
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        return cart;
    }

    private CartItem getCurrentUserCartItem(Long productId, Long variantId) {
        Cart cart = getCurrentUserCartOrThrow();
        return findCartItem(cart.getId(), productId, variantId)
                .orElseThrow(() -> new RuntimeException("Product is not in cart"));
    }

    private Optional<CartItem> findCartItem(Long cartId, Long productId, Long variantId) {
        return variantId != null
                ? cartItemRepository.findByCartIdAndProductIdAndVariantId(cartId, productId, variantId)
                : cartItemRepository.findByCartIdAndProductIdAndVariantIsNull(cartId, productId);
    }

    private Cart getOrCreateCurrentUserCart(User currentUser) {
        Cart existingCart = cartRepository.findByUserId(currentUser.getId());
        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUser(currentUser);
        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Cart cart, Product product, ProductVariant variant) {
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setVariant(variant);
        newItem.setQuantity(0);
        return newItem;
    }

    private ProductVariant resolveVariant(Product product, Long variantId) {
        if (variantId == null) {
            return null;
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

    private int getAvailableStock(Product product, ProductVariant variant) {
        return variant != null ? variant.getStockQuantity() : product.getStockQuantity();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}