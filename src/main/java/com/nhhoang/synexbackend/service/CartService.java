package com.nhhoang.synexbackend.service;

import com.nhhoang.synexbackend.model.Cart;
import com.nhhoang.synexbackend.model.CartItem;
import com.nhhoang.synexbackend.model.Product;
import com.nhhoang.synexbackend.model.User;
import com.nhhoang.synexbackend.repository.CartItemRepository;
import com.nhhoang.synexbackend.repository.CartRepository;
import com.nhhoang.synexbackend.repository.ProductRepository;
import com.nhhoang.synexbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItem addToCart(Long cartId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = resolveCart(cartId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int updatedQuantity = item.getQuantity() + quantity;
        if (product.getStockQuantity() < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        item.setCart(cart);
        item.setProduct(product);
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
        Cart existingCart = cartRepository.findByUserId(currentUser.getId());
        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUser(currentUser);
        return cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long productId) {
        CartItem item = getCurrentUserCartItem(productId);
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItem increaseItemQuantity(Long productId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = getCurrentUserCartItem(productId);
        Product product = item.getProduct();
        int updatedQuantity = item.getQuantity() + amount;

        if (product.getStockQuantity() < updatedQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(updatedQuantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem decreaseItemQuantity(Long productId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        CartItem item = getCurrentUserCartItem(productId);
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
        Cart cart = cartRepository.findByUserId(currentUser.getId());
        if (cart != null) {
            return cart;
        }

        Cart newCart = new Cart();
        newCart.setUser(currentUser);
        return cartRepository.save(newCart);
    }

    private Cart getCurrentUserCartOrThrow() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId());
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        return cart;
    }

    private CartItem getCurrentUserCartItem(Long productId) {
        Cart cart = getCurrentUserCartOrThrow();
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product is not in cart"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}